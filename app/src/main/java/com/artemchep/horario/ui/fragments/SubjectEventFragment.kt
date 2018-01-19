package com.artemchep.horario.ui.fragments

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.design.widget.AppBarLayout
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.artemchep.horario.Palette
import com.artemchep.horario.R
import com.artemchep.horario._new.UiHelper
import com.artemchep.horario._new.activities.SubjectActivity
import com.artemchep.horario.database.data.Event
import com.artemchep.horario.database.data.Subject
import com.artemchep.horario.database.models.IEvent
import com.artemchep.horario.database.models.ISubject
import com.artemchep.horario.extensions.setTextExclusive
import com.artemchep.horario.extensions.withAll
import com.artemchep.horario.interfaces.FragmentHost
import com.artemchep.horario.ui.utils.*
import com.artemchep.horario.utils.DateUtilz
import com.google.firebase.firestore.FirebaseFirestore
import com.thebluealliance.spectrum.internal.ColorUtil
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatterBuilder
import timber.log.Timber

/**
 * @author Artem Chepurnoy
 */
class SubjectEventFragment : FragmentDocument(), Toolbar.OnMenuItemClickListener {

    companion object {
        private const val TAG = "SubjectEventFragment"

        val DATE_FORMAT = DateTimeFormatterBuilder()
                .appendDayOfWeekShortText()
                .appendLiteral(", ")
                .appendMonthOfYearShortText()
                .appendLiteral(' ')
                .appendDayOfMonth(2)
                .appendLiteral(", ")
                .appendYear(4, 9)
                .toFormatter()
        val TIME_FORMAT = DateTimeFormat.mediumTime()
    }

    private var color: Int = 0
    private lateinit var userId: String
    private lateinit var subjectId: String
    private lateinit var scheduleId: String
    private lateinit var eventId: String

    private lateinit var subject: FireDocument<ISubject>
    private lateinit var subjectUser: FireDocument<ISubject>
    private lateinit var event: FireDocument<IEvent>

    private lateinit var appBar: AppBarLayout
    private lateinit var toolbar: Toolbar
    private lateinit var titleTextView: TextView

    private lateinit var timeStartView: View
    private lateinit var timeStartDateView: TextView
    private lateinit var timeStartTimeView: TextView
    private lateinit var timeEndView: View
    private lateinit var timeEndDateView: TextView
    private lateinit var timeEndTimeView: TextView
    private lateinit var timeRepeatView: View
    private lateinit var timeRepeatInfoView: TextView
    private lateinit var typeTextView: TextView
    private lateinit var teacherTextView: TextView
    private lateinit var placeTextView: TextView
    private lateinit var infoTextView: TextView

    private lateinit var editMenuItem: MenuItem
    private lateinit var removeMenuItem: MenuItem

    override fun onAttach(context: Context?) {
        arguments!!.run {
            userId = getString(EXTRA_USER_ID)
            subjectId = getString(EXTRA_SUBJECT_ID)
            scheduleId = getString(EXTRA_SCHEDULE_ID)
            eventId = getString(EXTRA_EVENT_ID)
            color = retrieveColor()
        }

        val msg = "Attach: user_id=$userId subject_id=$subjectId schedule_id=$scheduleId event_id=$eventId"
        Timber.tag(TAG).i(msg)

        super.onAttach(context)
    }

    override fun onLinkDatabase(manager: FireManager) {
        val firestore = FirebaseFirestore.getInstance()

        // Listen to the subject
        subject = FireDocument<ISubject>(
                firestore.document("subjects/$subjectId"), "subject",
                { snapshot -> Subject(id = snapshot.id).apply { inflateSubject(snapshot) } }
        ).also {
            it.value = arguments!!.getParcelable(EXTRA_SUBJECT)
            manager.link(it)
        }
        subjectUser = FireDocument<ISubject>(
                firestore.document("users/$userId/subjects/$subjectId"), "subject_user",
                { snapshot -> Subject(id = snapshot.id).apply { inflateSubject(snapshot) } }
        ).also {
            manager.link(it)
        }

        // Listen to the event
        event = FireDocument<IEvent>(
                firestore.document("subjects/$subjectId/schedules/$scheduleId/events/$eventId"), "event",
                { snapshot -> Event(id = snapshot.id).apply { inflateEvent(snapshot) } }
        ).also {
            it.value = arguments!!.getParcelable(EXTRA_EVENT)
            manager.link(it)
        }
    }

    override fun onCreateContentView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.___fragment_subject_schedule_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appBar = view.findViewById(R.id.appbar)
        toolbar = appBar.findViewById<Toolbar>(R.id.toolbar).apply {
            setNavigationOnClickListener { _ ->
                val host = activity as FragmentHost
                host.fragmentFinish(this@SubjectEventFragment)
            }
            // Setup menu
            inflateMenu(R.menu.___event)
            setOnMenuItemClickListener(this@SubjectEventFragment)
            editMenuItem = menu.findItem(R.id.action_edit)!!
            removeMenuItem = menu.findItem(R.id.action_remove)!!
        }
        titleTextView = appBar.findViewById(R.id.title)

        setupColorAccent(color)

        // Time start
        timeStartView = view.findViewById<View>(R.id.time_start).apply {
            timeStartDateView = findViewById(R.id.time_start_date)
            timeStartTimeView = findViewById(R.id.time_start_time)
        }
        // Time end
        timeEndView = view.findViewById<View>(R.id.time_end).apply {
            timeEndDateView = findViewById(R.id.time_end_date)
            timeEndTimeView = findViewById(R.id.time_end_time)
        }
        // Time repeat
        timeRepeatView = view.findViewById<View>(R.id.time_repeat).apply {
            timeRepeatInfoView = findViewById(R.id.time_repeat_info)
        }

        typeTextView = view.findViewById(R.id.type)
        teacherTextView = view.findViewById(R.id.teacher)
        placeTextView = view.findViewById(R.id.place)
        infoTextView = view.findViewById(R.id.info)
    }

    private fun setupColorAccent(@ColorInt color: Int) {
        appBar.setBackgroundColor(color)
        UiHelper.updateToolbarTitle(toolbar, color)
        UiHelper.updateToolbarBackIcon(toolbar, color)
        UiHelper.updateToolbarMenuIcons(toolbar, color, R.id.action_edit, R.id.action_remove)

        val colorDark = ColorUtil.isColorDark(color)
        titleTextView.setTextColor(if (colorDark) Color.WHITE else Color.BLACK)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        /**
         * Refreshes the state of the
         * [editButton][edit button].
         */
        val refreshTitle: (IEvent?, ISubject?, ISubject?) -> Unit = { event, subject, subjectUser ->
            titleTextView.text = event?.title?.takeUnless { it.isBlank() } ?: (subjectUser ?: subject)?.name
        }

        event.observer.follow { event ->
            removeMenuItem.isVisible = event?.author == userId

            // Update time
            if (event != null) {
                withAll({ visibility = View.VISIBLE }, timeStartView, timeEndView, timeRepeatView)

                timeStartDateView.text = DateTime(event.dateStart).toString(DATE_FORMAT)
                timeEndDateView.text = DateTime(event.dateEnd).toString(DATE_FORMAT)

                timeStartTimeView.text = DateUtilz.format(event.timeStart / 60, event.timeStart % 60)
                timeEndTimeView.text = DateUtilz.format(event.timeEnd / 60, event.timeEnd % 60)

                when (event.repeatType) {
                    Event.REPEAT_OFF -> timeRepeatInfoView.text = getString(R.string.event_repeat_off)
                    else -> {
                        val day = when (event.repeatType) {
                            Event.REPEAT_DAILY -> resources.getQuantityString(R.plurals.days, event.repeatEvery)
                            Event.REPEAT_WEEKLY -> resources.getQuantityString(R.plurals.weeks, event.repeatEvery)
                            else -> resources.getQuantityString(R.plurals.months, event.repeatEvery)
                        }
                        timeRepeatInfoView.text = getString(R.string.event_repeat_on, event.repeatEvery, day)
                    }
                }
            } else {
                withAll({ visibility = View.GONE }, timeStartView, timeEndView, timeRepeatView)
            }

            // Update other
            placeTextView.setTextExclusive(event?.place)
            teacherTextView.setTextExclusive(event?.teacher)
            infoTextView.setTextExclusive(event?.info)
            typeTextView.setTextExclusive(event?.type?.let {
                when (it) {
                    Event.TYPE_LECTURE -> getString(R.string.lecture)
                    Event.TYPE_PRACTICE -> getString(R.string.practice)
                    Event.TYPE_SEMINAR -> getString(R.string.seminar)
                    Event.TYPE_LAB -> getString(R.string.lab)
                    else -> null
                }
            })

            // Update title
            refreshTitle(event, subject.value, subjectUser.value)
        }

        subject.observer.follow { subject ->
            refreshTitle(event.value, subject, subjectUser.value)
        }

        subjectUser.observer.follow { subjectUser ->
            refreshTitle(event.value, subject.value, subjectUser)
        }

        manager.state.follow { state ->
            editMenuItem.isVisible = state == FireManager.SUCCESS && event.value?.author == userId
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_edit -> event.value?.let {
                val args = SubjectEventEditFragment.args(
                        userId = userId,
                        subjectId = subjectId,
                        scheduleId = scheduleId,
                        event = it,
                        color = color)

                val host = activity as FragmentHost
                host.fragmentShow(SubjectEventEditFragment::class.java, args, 0)
            }
            R.id.action_open_subject -> {
                val intent = Intent(context, SubjectActivity::class.java).apply {
                    putExtra(EXTRA_USER_ID, userId)
                    putExtra(EXTRA_SUBJECT_ID, subjectId)
                }

                startActivity(intent)
            }
            R.id.action_open_schedule -> {
                // Open the schedule of this event.
                val args = Bundle().apply {
                    putInt(EXTRA_COLOR, color)
                    putString(EXTRA_USER_ID, userId)
                    putString(EXTRA_SUBJECT_ID, subjectId)
                    putString(EXTRA_SCHEDULE_ID, scheduleId)
                }

                val host = activity as FragmentHost
                host.fragmentShow(SubjectScheduleFragment::class.java, args, FragmentHost.FLAG_AS_DIALOG)
            }
            R.id.action_remove -> {
                val md = MaterialDialog.Builder(context!!)
                        .content(R.string.event_remove_q)
                        .positiveText(R.string.action_remove)
                        .negativeText(android.R.string.cancel)
                        .onPositive { _, _ ->
                            val firestore = FirebaseFirestore.getInstance()
                            firestore.document("subjects/$subjectId/schedules/$scheduleId/events/$eventId").delete()

                            // Finish the fragment
                            val host = activity as FragmentHost
                            host.fragmentFinish(this)
                        }
                        .build()

                md.getActionButton(DialogAction.POSITIVE).setTextColor(Palette.RED)
                md.show()
            }
            else -> return false
        }
        return true
    }

}