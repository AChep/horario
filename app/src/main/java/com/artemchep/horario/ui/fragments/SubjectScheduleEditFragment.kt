package com.artemchep.horario.ui.fragments

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.design.widget.TextInputLayout
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.artemchep.horario.Palette
import com.artemchep.horario.R
import com.artemchep.horario._new.UiHelper
import com.artemchep.horario.database.data.Schedule
import com.artemchep.horario.database.generateKey
import com.artemchep.horario.database.models.ISchedule
import com.artemchep.horario.interfaces.FragmentHost
import com.artemchep.horario.ui.activities.ChildActivity
import com.artemchep.horario.ui.utils.EXTRA_COLOR
import com.artemchep.horario.ui.utils.EXTRA_SCHEDULE
import com.artemchep.horario.ui.utils.EXTRA_SUBJECT_ID
import com.artemchep.horario.ui.utils.EXTRA_USER_ID
import com.google.firebase.database.DataSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import org.sufficientlysecure.htmltextview.HtmlTextView
import timber.log.Timber

/**
 * @author Artem Chepurnoy
 */
class SubjectScheduleEditFragment : FragmentBase(),
        View.OnClickListener,
        Toolbar.OnMenuItemClickListener {

    companion object {
        private const val TAG: String = "SubjectScheduleEditFragment"
        private const val STATE_SCHEDULE: String = "state::schedule"
        private const val RC_EDITOR_TEXT: Int = 19

        /**
         * Create the bundle of required arguments for this
         * fragment.
         */
        fun args(userId: String,
                 subjectId: String,
                 schedule: ISchedule,
                 color: Int? = null
        ): Bundle = Bundle().apply {
            putString(EXTRA_USER_ID, userId)
            putString(EXTRA_SUBJECT_ID, subjectId)
            putParcelable(EXTRA_SCHEDULE, schedule)
            color?.also { putInt(EXTRA_COLOR, it) }
        }
    }

    data class ThemeColors(
            val textPrimary: Int,
            val textHint: Int
    )

    private lateinit var userId: String
    private lateinit var subjectId: String
    private var color: Int = Palette.GREY

    /**
     * Schedule passed as parameter to this class. Used
     * to determinate changes.
     */
    private lateinit var origin: Schedule
    private lateinit var schedule: Schedule

    private lateinit var styles: ThemeColors

    private lateinit var appBar: AppBarLayout
    private lateinit var toolbar: Toolbar
    private lateinit var fab: FloatingActionButton
    private lateinit var descriptionHtmlView: HtmlTextView
    private lateinit var nameInputLayout: TextInputLayout

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        arguments!!.run {
            userId = getString(EXTRA_USER_ID)
            subjectId = getString(EXTRA_SUBJECT_ID)
            color = getInt(EXTRA_COLOR, Palette.GREY) or Color.BLACK
        }

        Timber.tag(TAG).i("Attach: user_id=" + userId)

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
        val s: Schedule? = arguments!!.getParcelable(EXTRA_SCHEDULE)
        origin = s?.copy() ?: Schedule()
        schedule = if (savedState != null) {
            savedState.getParcelable(STATE_SCHEDULE)
        } else {
            // Copy everything except author and generate
            // key if empty
            origin.copy(author = userId).apply {
                if (id.isNullOrEmpty()) id = generateKey()
            }
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.___fragment_subject_schedule__edit, container, false)
    }

    override fun onViewCreated(view: View, savedState: Bundle?) {
        super.onViewCreated(view, savedState)

        appBar = view.findViewById(R.id.appbar)
        toolbar = appBar.findViewById<Toolbar>(R.id.toolbar).apply {
            setTitle(if (origin.id.isNullOrEmpty()) {
                R.string.__subject_schedule_new
            } else R.string.__subject_schedule_edit)
            // Setup menu
            setOnMenuItemClickListener(this@SubjectScheduleEditFragment)
            inflateMenu(R.menu.___subject_schedule__edit).also {
                menu.findItem(R.id.action_delete).isVisible = !origin.id.isNullOrEmpty()
            }
        }

        fab = view.findViewById<FloatingActionButton>(R.id.fab).apply {
            // Hide fab if no name set
            if (schedule.name.isNullOrEmpty()) hide()
            // Update the icon
            setImageResource(if (origin.id.isNullOrEmpty()) {
                R.drawable.ic_publish_white_24dp
            } else R.drawable.ic_content_save_white_24dp)
        }

        descriptionHtmlView = view.findViewById(R.id.info)
        nameInputLayout = view.findViewById(R.id.subject_name_input_layout)
        nameInputLayout.editText!!.apply {
            setText(schedule.name)
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

                override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

                override fun afterTextChanged(editable: Editable) {
                    if (editable.isEmpty()) {
                        fab.hide()
                        // Show error hint
                        val errorMsg = getString(R.string.dialog_subject_error_enter_name)
                        nameInputLayout.error = errorMsg
                    } else {
                        fab.show()
                        nameInputLayout.isErrorEnabled = false
                    }
                }
            })
        }

        view.findViewById<View>(R.id.info_container).setOnClickListener(this)
        view.findViewById<View>(R.id.fab).setOnClickListener(this)

        appBar.setBackgroundColor(color)
        UiHelper.updateTextInputLayout(nameInputLayout, color)
        UiHelper.updateToolbarTitle(toolbar, color)
        UiHelper.updateToolbarBackIcon(toolbar, color)

        performSetDescription(schedule.description)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.fab -> {
                updateSchedule()

                val saved = dbSave()
                if (saved.not()) {
                    return
                }

                // Finish the fragment
                val host = activity as FragmentHost
                host.fragmentFinish(this@SubjectScheduleEditFragment)
            }
            R.id.info_container -> {
                val title = getString(R.string.hint_description)
                val text = schedule.description
                val args = Bundle().apply {
                    putString(RichEditorFragment.EXTRA_TITLE, title)
                    putString(RichEditorFragment.EXTRA_HTML, text)
                }

                val intent = ChildActivity.makeFor(context!!, RichEditorFragment::class.java, args)
                startActivityForResult(intent, RC_EDITOR_TEXT)
            }
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete -> {
                val md = MaterialDialog.Builder(context!!)
                        .title("Remove schedule permanently?")
                        .positiveText(R.string.action_remove)
                        .negativeText(android.R.string.cancel)
                        .onPositive { _, _ ->
                            if (origin.id.isNullOrEmpty()) {
                                Timber.wtf("Tried to remove non-existent schedule!")
                                return@onPositive
                            }

                            dbRemove()

                            // Finish the fragment
                            val host = activity as FragmentHost
                            host.fragmentFinish(this@SubjectScheduleEditFragment)
                        }
                        .build()

                md.getActionButton(DialogAction.POSITIVE).setTextColor(Palette.RED)
                md.show()
            }
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) when (requestCode) {
            RC_EDITOR_TEXT -> {
                if (resultCode == RichEditorFragment.RC_SAVED) {
                    val text = data.getStringExtra(RichEditorFragment.EXTRA_HTML)
                    setDescription(text)
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        updateSchedule()
        outState.putParcelable(STATE_SCHEDULE, schedule)
    }

    private fun dbSave(): Boolean {
        val process: String?.() -> String? = {
            this?.trim()
                    ?.replace("\n", "")
                    ?.replace("\r", "")
        }

        schedule.name = schedule.name?.process()
        schedule.description = schedule.description?.trim()

        nameInputLayout.editText!!.setText(schedule.name)

        // Client-side data rules
        if (schedule.name.isNullOrBlank()) {
            return false
        }

        val firestore = FirebaseFirestore.getInstance()
        val doc = firestore.document("subjects/$subjectId/schedules/" + schedule.id!!)

        if (!origin.id.isNullOrEmpty()) { // update schedule
            val curr: MutableMap<String, Any?> = schedule.deflateSchedule()
            val prev: MutableMap<String, Any?> = origin.deflateSchedule()

            // Update only changed entries
            val map = curr.filter { prev[it.key] != it.value }
            map.takeUnless { map.isEmpty() }?.also {
                Timber.tag(TAG).i("Subject UPDATE: [$map]")
                doc.update(it)
            }
        } else { // create schedule
            val map = schedule.deflateSchedule().filter { it.value != null }
            Timber.tag(TAG).i("Subject CREATE: [$map]")
            doc.set(map)
        }

        return true
    }

    private fun dbRemove() {
        val firestore = FirebaseFirestore.getInstance()
        val doc = firestore.document("subjects/$subjectId/schedules/" + schedule.id!!)

        doc.delete()
    }

    private fun updateSchedule() {
        schedule.name = nameInputLayout.editText!!.text.toString()
    }

    private fun setDescription(text: String?) {
        val message = getString(R.string.snackbar_description_changed)
        setValueWithUndo({ it.description }, { performSetDescription(it) }, text, message)
    }

    private fun performSetDescription(text: String?) {
        schedule.description = text
        descriptionHtmlView.apply {
            if (text.isNullOrBlank()) {
                setText(R.string.hint_description)
                setTextColor(styles.textHint)
            } else {
                setHtml(text!!)
                setTextColor(styles.textPrimary)
                movementMethod = null // clickable
            }
        }
    }

    private fun <T> setValueWithUndo(get: (Schedule) -> T, set: (T) -> Unit, value: T, text: String) {
        val previous = get.invoke(schedule)
        set.invoke(value)
        Snackbar.make(appBar, text, Snackbar.LENGTH_LONG)
                .setAction(R.string.snackbar_undo) { set.invoke(previous) }
                .show()
    }

}
