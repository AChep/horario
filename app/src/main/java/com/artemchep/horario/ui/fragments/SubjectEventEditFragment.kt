package com.artemchep.horario.ui.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.design.widget.AppBarLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.TextInputLayout
import android.support.v7.widget.AppCompatSpinner
import android.support.v7.widget.Toolbar
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.afollestad.materialdialogs.MaterialDialog
import com.artemchep.horario.Palette
import com.artemchep.horario.R
import com.artemchep.horario._new.BackHelper
import com.artemchep.horario._new.UiHelper
import com.artemchep.horario.database.data.Event
import com.artemchep.horario.database.generateKey
import com.artemchep.horario.database.models.IEvent
import com.artemchep.horario.extensions.withOnClick
import com.artemchep.horario.interfaces.FragmentHost
import com.artemchep.horario.ui.utils.*
import com.artemchep.horario.ui.widgets.SimpleNumberPicker
import com.artemchep.horario.utils.DateUtilz
import com.google.firebase.firestore.FirebaseFirestore
import org.joda.time.DateTime
import org.joda.time.DateTimeUtils
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import timber.log.Timber

/**
 * @author Artem Chepurnoy
 */
class SubjectEventEditFragment : FragmentBase(), View.OnClickListener {

    companion object {
        private const val TAG = "SubjectEventEditFragment"
        private const val STATE_EVENT = "state::event"

        /**
         * Create the bundle of required arguments for this
         * fragment.
         */
        fun args(userId: String,
                 subjectId: String,
                 scheduleId: String,
                 event: IEvent? = null,
                 color: Int? = null
        ): Bundle = Bundle().apply {
            putString(EXTRA_USER_ID, userId)
            putString(EXTRA_SUBJECT_ID, subjectId)
            putString(EXTRA_SCHEDULE_ID, scheduleId)
            putParcelable(EXTRA_EVENT, event)
            color?.also { putInt(EXTRA_COLOR, it) }
        }
    }

    private var color: Int = 0
    private lateinit var userId: String
    private lateinit var subjectId: String
    private lateinit var scheduleId: String

    /**
     * Event passed as parameter to this class. Used
     * to determinate changes.
     */
    private lateinit var origin: Event
    private lateinit var event: Event

    private lateinit var appBar: AppBarLayout
    private lateinit var toolbar: Toolbar
    private lateinit var titleInputLayout: TextInputLayout
    private lateinit var fab: FloatingActionButton

    private val mBackHelper = BackHelper()

    private lateinit var styles: ThemeColors

    private lateinit var timeStartDateView: TextView
    private lateinit var timeStartTimeView: TextView
    private lateinit var timeEndDateView: TextView
    private lateinit var timeEndTimeView: TextView
    private lateinit var timeRepeatInfoView: TextView
    private lateinit var typeTextView: TextView
    private lateinit var typeClearView: View

    private lateinit var teacherEditText: EditText
    private lateinit var placeEditText: EditText
    private lateinit var infoEditText: EditText

    data class ThemeColors(
            val textPrimary: Int,
            val textHint: Int
    )

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        arguments!!.run {
            userId = getString(EXTRA_USER_ID)
            subjectId = getString(EXTRA_SUBJECT_ID)
            scheduleId = getString(EXTRA_SCHEDULE_ID)
            color = getInt(EXTRA_COLOR, Palette.GREY) or Color.BLACK
        }

        val msg = "Attach: user_id=$userId subject_id=$subjectId schedule_id=$scheduleId"
        Timber.tag(TAG).i(msg)

        // Get the primary and secondary
        // description colors of the theme
        activity!!.obtainStyledAttributes(TypedValue().also {
            activity!!.theme.run {
                resolveAttribute(android.R.attr.textColorPrimary, it, true)
                resolveAttribute(android.R.attr.textColorHint, it, true)
            }
        }.data, intArrayOf(android.R.attr.textColorPrimary, android.R.attr.textColorHint)).also {
            styles = ThemeColors(
                    textPrimary = it.getColor(0, Color.GRAY),
                    textHint = it.getColor(1, Color.GRAY)
            )
        }.recycle()
    }

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        val s: Event? = arguments!!.getParcelable(EXTRA_EVENT)
        origin = s?.copy() ?: Event()
        event = if (savedState != null) {
            savedState.getParcelable(STATE_EVENT)
        } else {
            // Copy everything and pre-fill some information
            origin.copy(author = userId).apply {
                if (id.isNullOrEmpty()) {
                    id = generateKey()
                    // time
                    timeStart = 13 * 60 // 13:00
                    timeEnd = 14 * 60 + 30 // 14:30
                    // today
                    val now = DateTimeUtils.currentTimeMillis()
                    dateStart = now
                    dateEnd = now
                }
            }
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.___fragment_subject_schedule_event__edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fab = view.findViewById<FloatingActionButton>(R.id.fab).withOnClick(this).apply {
            // Update the icon
            setImageResource(if (origin.id.isNullOrEmpty()) {
                R.drawable.ic_publish_white_24dp
            } else R.drawable.ic_content_save_white_24dp)
        }
        appBar = view.findViewById(R.id.appbar)
        toolbar = appBar.findViewById<Toolbar>(R.id.toolbar).apply {
            setNavigationOnClickListener { _ ->
                val host = activity as FragmentHost
                host.fragmentFinish(this@SubjectEventEditFragment)
            }
        }
        titleInputLayout = appBar.findViewById<TextInputLayout>(R.id.title_input_layout).apply {
            editText!!.apply {
                //                subjectName.takeUnless { it.isNullOrBlank() }?.let { hint = it }
                setText(event.title)
            }
        }

        setupColorAccent(color)

        // Time start
        view.findViewById<View>(R.id.time_start).withOnClick(this).run {
            timeStartDateView = findViewById(R.id.time_start_date)
            timeStartTimeView = findViewById<TextView>(R.id.time_start_time).withOnClick(this@SubjectEventEditFragment)
        }
        // Time end
        view.findViewById<View>(R.id.time_end).withOnClick(this).apply {
            timeEndDateView = findViewById(R.id.time_end_date)
            timeEndTimeView = findViewById<TextView>(R.id.time_end_time).withOnClick(this@SubjectEventEditFragment)
        }
        // Time repeat
        view.findViewById<View>(R.id.time_repeat).withOnClick(this).apply {
            timeRepeatInfoView = findViewById(R.id.time_repeat_info)
        }
        // Type
        view.findViewById<View>(R.id.type_container).withOnClick(this).apply {
            typeTextView = findViewById(R.id.type)
            typeClearView = findViewById<View>(R.id.type_clear).withOnClick(this@SubjectEventEditFragment)
        }

        teacherEditText = view.findViewById(R.id.teacher)
        teacherEditText.setText(event.teacher)
        placeEditText = view.findViewById(R.id.place)
        placeEditText.setText(event.place)
        infoEditText = view.findViewById(R.id.info)
        infoEditText.setText(event.info)

        setDateStart(event.dateStart)
        setTimeStart(event.timeStart)
        setDateEnd(event.dateEnd)
        setTimeEnd(event.timeEnd)
        setTimeRepeat(event.repeatType, event.repeatEvery)
        setType(event.type)

        updateTimeCheck()
    }

    private fun setupColorAccent(@ColorInt color: Int) {
        appBar.setBackgroundColor(color)
        UiHelper.updateToolbarTitle(toolbar, color)
        UiHelper.updateToolbarCloseIcon(toolbar, color)
        UiHelper.updateTextInputLayout(titleInputLayout, color)
    }

    private fun setDateStart(dateStart: Long) {
        event.dateStart = dateStart
        timeStartDateView.text = DateTime(dateStart).toString(SubjectEventFragment.DATE_FORMAT)
    }

    private fun setTimeStart(timeStart: Int) {
        event.timeStart = timeStart
        val h = timeStart / 60
        val m = timeStart % 60
        timeStartTimeView.text = DateUtilz.format(h, m)
    }

    private fun setDateEnd(dateEnd: Long) {
        event.dateEnd = dateEnd
        timeEndDateView.text = DateTime(dateEnd).toString(SubjectEventFragment.DATE_FORMAT)
    }

    private fun setTimeEnd(timeEnd: Int) {
        event.timeEnd = timeEnd
        val h = timeEnd / 60
        val m = timeEnd % 60
        timeEndTimeView.text = DateUtilz.format(h, m)
    }

    private fun setTimeRepeat(type: Int, every: Int) {
        event.repeatType = if (every > 0) type else Event.REPEAT_OFF
        event.repeatEvery = every

        when (type) {
            Event.REPEAT_OFF -> timeRepeatInfoView.text = getString(R.string.event_repeat_off)
            else -> {
                val day = when (type) {
                    Event.REPEAT_DAILY -> resources.getQuantityString(R.plurals.days, every)
                    Event.REPEAT_WEEKLY -> resources.getQuantityString(R.plurals.weeks, every)
                    else -> resources.getQuantityString(R.plurals.months, every)
                }
                timeRepeatInfoView.text = getString(R.string.event_repeat_on, every, day)
            }
        }
    }

    private fun setType(type: Int) {
        event.type = type
        typeTextView.text = getString(when (type) {
            Event.TYPE_LAB -> R.string.lab
            Event.TYPE_LECTURE -> R.string.lecture
            Event.TYPE_PRACTICE -> R.string.practice
            Event.TYPE_SEMINAR -> R.string.seminar
            else -> R.string.hint_type
        })

        when (type) {
            Event.TYPE_LAB,
            Event.TYPE_LECTURE,
            Event.TYPE_PRACTICE,
            Event.TYPE_SEMINAR -> {
                typeClearView.visibility = View.VISIBLE
                typeTextView.setTextColor(styles.textPrimary)
            }
            else -> {
                typeClearView.visibility = View.GONE
                typeTextView.setTextColor(styles.textHint)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        updateCurrentEvent()
        outState.putParcelable(EXTRA_EVENT, event)
    }

    private fun updateCurrentEvent() {
        event.teacher = teacherEditText.text.toString()
        event.place = placeEditText.text.toString()
        event.info = infoEditText.text.toString()

        // Update title description
        val editText = titleInputLayout.editText!!
        event.title = editText.text.toString()
    }

    private fun updateTimeCheck() {
        val start = LocalDate(event.dateStart).run {
            val m = event.timeStart % 60
            val h = event.timeStart / 60
            toDateTime(LocalTime(h, m))
        }
        val end = LocalDate(event.dateEnd).run {
            val m = event.timeEnd % 60
            val h = event.timeEnd / 60
            toDateTime(LocalTime(h, m))
        }

        if (start.isAfter(end)) {
            fab.hide()
            timeEndDateView.setTextColor(Palette.RED)
            timeEndTimeView.setTextColor(Palette.RED)
        } else {
            fab.show()
            timeEndDateView.setTextColor(styles.textPrimary)
            timeEndTimeView.setTextColor(styles.textPrimary)
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.time_start -> selectDateStart(view)
            R.id.time_start_time -> selectTimeStart(view)
            R.id.time_end -> selectDateEnd(view)
            R.id.time_end_time -> selectTimeEnd(view)
            R.id.time_repeat -> selectTimeRepeat()
            R.id.type_clear -> setType(0)
            R.id.type_container -> selectType(view)
            R.id.fab -> {
                updateCurrentEvent()

                val saved = dbSave()
                if (saved.not()) {
                    return
                }

                // Finish the fragment
                val host = activity as FragmentHost
                host.fragmentFinish(this)
            }
        }
    }

    private fun dbSave(): Boolean {
        val process: String?.() -> String? = {
            this?.trim()
                    ?.replace("\n", "")
                    ?.replace("\r", "")
        }

        event.title = event.title?.process()
        event.teacher = event.teacher?.process()
        event.place = event.place?.process()
        event.info = event.info?.trim()

        titleInputLayout.editText!!.setText(event.title)
        teacherEditText.setText(event.teacher)
        placeEditText.setText(event.place)
        infoEditText.setText(event.info)

        // Client-side data rules
        val start = LocalDate(event.dateStart).run {
            val m = event.timeStart % 60
            val h = event.timeStart / 60
            toDateTime(LocalTime(h, m))
        }
        val end = LocalDate(event.dateEnd).run {
            val m = event.timeEnd % 60
            val h = event.timeEnd / 60
            toDateTime(LocalTime(h, m))
        }
        if (start.isAfter(end)) {
            return false
        }

        val firestore = FirebaseFirestore.getInstance()
        val doc = firestore.document("subjects/$subjectId/schedules/$scheduleId/events/" + event.id!!)

        if (!origin.id.isNullOrEmpty()) { // update event
            val curr = event.deflateEvent()
            val prev = origin.deflateEvent()

            // Update only changed entries
            val map = curr.filter { prev[it.key] != it.value }
            map.takeUnless { map.isEmpty() }?.also {
                Timber.tag(TAG).i("Event UPDATE: [$map]")
                doc.update(it)
            }
        } else { // create event
            val map = event.deflateEvent().filter { it.value != null }
            map.also {
                Timber.tag(TAG).i("Event CREATE: [$map]")
                doc.set(it)
            }
        }

        return true
    }

    /**
     * Shows start-date selection dialog
     * from a view.
     */
    private fun selectDateStart(view: View) {
        val date = LocalDate(event.dateStart)
        DatePickerDialog(context, { _, year, monthOfYear, dayOfMonth ->
            val now = LocalDate(year, monthOfYear + 1, dayOfMonth).toDate()
            val delta = now.time - event.dateStart
            setDateStart(event.dateStart + delta)
            setDateEnd(event.dateEnd + delta)
        }, date.year, date.monthOfYear - 1, date.dayOfMonth).show()
    }

    /**
     * Shows start-time selection dialog
     * from a view.
     */
    private fun selectTimeStart(view: View) {
        val m = event.timeStart % 60
        val h = event.timeStart / 60
        TimePickerDialog(activity, { _, hourOfDay, minute ->
            setTimeStart(hourOfDay * 60 + minute)
            updateTimeCheck()
        }, h, m, true).show()
    }

    /**
     * Shows end-date selection dialog
     * from a view.
     */
    private fun selectDateEnd(view: View) {
        val date = LocalDate(event.dateEnd)
        DatePickerDialog(context, { _, year, monthOfYear, dayOfMonth ->
            setDateEnd(LocalDate(year, monthOfYear + 1, dayOfMonth).toDate().time)
            updateTimeCheck()
        }, date.year, date.monthOfYear - 1, date.dayOfMonth).show()
    }

    /**
     * Shows end-time selection dialog
     * from a view.
     */
    private fun selectTimeEnd(view: View) {
        val m = event.timeEnd % 60
        val h = event.timeEnd / 60
        TimePickerDialog(activity, { _, hourOfDay, minute ->
            setTimeEnd(hourOfDay * 60 + minute)
            updateTimeCheck()
        }, h, m, true).show()
    }

    private fun selectTimeRepeat() {
        var type = event.repeatType
        var every = Math.max(event.repeatEvery, 1)

        // Create material dialog
        val md = MaterialDialog.Builder(context!!)
                .customView(R.layout.___dialog_event_repeat, false)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .onPositive { dialog, _ ->
                    val view = dialog.customView!!
                    if (view.findViewById<Switch>(R.id.switchy).isChecked) {
                        setTimeRepeat(type, every)
                    } else setTimeRepeat(Event.REPEAT_OFF, 1)
                }
                .build()

        val view = md.customView!!
        val refresh = {
            view.findViewById<TextView>(R.id.poor_guy).apply {
                val day = when (type) {
                    Event.REPEAT_WEEKLY -> resources.getQuantityString(R.plurals.weeks, every)
                    Event.REPEAT_MONTHLY -> resources.getQuantityString(R.plurals.months, every)
                    else -> resources.getQuantityString(R.plurals.days, every)
                }

                text = day
            }
        }

        // Init spinner
        view.findViewById<AppCompatSpinner>(R.id.spinner).apply {
            val entries = resources.getStringArray(R.array.repeat_types)
            val adapter = ArrayAdapter<String>(context, R.layout.___item_spinner_title, entries)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            setAdapter(adapter)
            setSelection(when (type) {
                Event.REPEAT_DAILY -> 0
                Event.REPEAT_MONTHLY -> 2
                else -> 1 // weekly by default
            })

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(adapterView: AdapterView<*>, v: View, i: Int, l: Long) {
                    type = when (selectedItemPosition) {
                        0 -> Event.REPEAT_DAILY
                        1 -> Event.REPEAT_WEEKLY
                        2 -> Event.REPEAT_MONTHLY
                        else -> return
                    }

                    refresh()
                }

                override fun onNothingSelected(adapterView: AdapterView<*>) {}
            }
        }

        // Init number-picker
        view.findViewById<SimpleNumberPicker>(R.id.np).apply {
            minValue = 1
            maxValue = 42

            value.value = Math.max(Math.min(every, maxValue), minValue)
            value.follow { v ->
                every = v
                refresh()
            }
        }

        // Init switch
        view.findViewById<Switch>(R.id.switchy).apply { isChecked = true }

        md.show()
    }

    private fun selectType(view: View) {
        // Form popup menu and show it
        val popup = PopupMenu(activity, typeTextView)
        var i = 1
        for (d in arrayOf(
                getString(R.string.lecture),
                getString(R.string.practice),
                getString(R.string.seminar),
                getString(R.string.lab)
        )) popup.menu.add(0, i++, 0, d)
        popup.setOnMenuItemClickListener { item ->
            setType(item.itemId)
            false
        }
        popup.show()
    }

}
