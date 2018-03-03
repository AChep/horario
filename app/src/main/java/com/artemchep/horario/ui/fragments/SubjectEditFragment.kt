package com.artemchep.horario.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.design.widget.AppBarLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.design.widget.TextInputLayout
import android.support.v7.view.menu.ActionMenuItemView
import android.support.v7.widget.SwitchCompat
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.artemchep.horario.Binfo
import com.artemchep.horario.Config
import com.artemchep.horario.Palette
import com.artemchep.horario.Palette.PALETTE
import com.artemchep.horario.R
import com.artemchep.horario._new.UiHelper
import com.artemchep.horario.database.data.Subject
import com.artemchep.horario.database.generateKey
import com.artemchep.horario.extensions.findMenuItemView
import com.artemchep.horario.interfaces.FragmentHost
import com.artemchep.horario.ui.activities.ChildActivity
import com.artemchep.horario.ui.utils.EXTRA_SUBJECT
import com.artemchep.horario.ui.utils.EXTRA_USER_ID
import com.google.firebase.firestore.FirebaseFirestore
import com.thebluealliance.spectrum.SpectrumPalette
import org.sufficientlysecure.htmltextview.HtmlTextView
import timber.log.Timber
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt

/**
 * @author Artem Chepurnoy
 */
class SubjectEditFragment : FragmentBase(),
        View.OnClickListener,
        Toolbar.OnMenuItemClickListener,
        SpectrumPalette.OnColorSelectedListener {

    companion object {
        private const val TAG = "SubjectEditFragment"
        private const val STATE_SUBJECT = "state::subject"
        private const val REQUEST_INFO = 228
    }

    data class ThemeColors(
            val textPrimary: Int,
            val textHint: Int
    )

    private lateinit var firestore: FirebaseFirestore
    private lateinit var userId: String

    /**
     * Subject passed as parameter to this class. Used
     * to determinate changes.
     */
    private lateinit var origin: Subject
    private lateinit var subject: Subject

    @ColorInt
    private var colorTemp: Int = 0

    private lateinit var styles: ThemeColors

    private lateinit var fab: FloatingActionButton
    private lateinit var appBar: AppBarLayout
    private lateinit var toolBar: Toolbar
    private lateinit var readOnlySwitch: SwitchCompat
    private lateinit var nameInputLayout: TextInputLayout
    private lateinit var descriptionHtmlView: HtmlTextView

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        firestore = FirebaseFirestore.getInstance()
        arguments!!.run {
            userId = getString(EXTRA_USER_ID)
        }

        Timber.tag(TAG).d("Attach: user_id=$userId")

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
        val s: Subject? = arguments!!.getParcelable(EXTRA_SUBJECT)
        origin = s?.copy() ?: Subject()
        subject = if (savedState != null) {
            savedState.getParcelable(STATE_SUBJECT)
        } else {
            // Copy everything except author and generate
            // key if empty
            origin.copy(author = userId).apply {
                if (id.isNullOrEmpty()) {
                    id = generateKey()
                    color = Palette.GREY
                }
            }
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.___fragment_subject__edit, container, false)
    }

    override fun onViewCreated(view: View, savedState: Bundle?) {
        super.onViewCreated(view, savedState)
        fab = view.findViewById<FloatingActionButton>(R.id.fab).apply {
            // Hide fab if no name set
            if (subject.name.isNullOrEmpty()) hide()
            // Update the icon
            setImageResource(if (origin.id.isNullOrEmpty()) {
                R.drawable.ic_publish_white_24dp
            } else R.drawable.ic_content_save_white_24dp)
        }

        appBar = view.findViewById(R.id.appbar)
        toolBar = appBar.findViewById<Toolbar>(R.id.toolbar).apply {
            inflateMenu(R.menu.___subject__edit)
            setOnMenuItemClickListener(this@SubjectEditFragment)

            menu.run {
                if (origin.id.isNullOrEmpty()) {
                    findItem(R.id.action_delete)!!.isVisible = false
                }
            }
        }
        readOnlySwitch = view.findViewById<SwitchCompat>(R.id.readonly).apply {
            isChecked = subject.archived
        }
        descriptionHtmlView = view.findViewById(R.id.info)
        nameInputLayout = view.findViewById<TextInputLayout>(R.id.subject_name).apply {
            editText!!.setText(subject.name)
            editText!!.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

                override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

                override fun afterTextChanged(editable: Editable) {
                    if (editable.isBlank()) {
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

        view.findViewById<View>(R.id.fab).setOnClickListener(this)
        view.findViewById<View>(R.id.info_container).setOnClickListener(this)
        view.findViewById<View>(R.id.readonly_container).setOnClickListener(this)

        performSetDescription(subject.description)
        performSetColor(subject.color)
    }

    override fun onResume() {
        super.onResume()

        promptPaletteMenuItem()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.fab -> {
                updateCurrentSubject()

                val saved = dbSave()
                if (saved.not()) {
                    return
                }

                // Finish the fragment
                val host = activity as FragmentHost
                host.fragmentFinish(this@SubjectEditFragment)
            }
            R.id.info_container -> {
                val args = Bundle().apply {
                    val title = getString(R.string.hint_description)
                    putString(RichEditorFragment.EXTRA_HTML, subject.description)
                    putString(RichEditorFragment.EXTRA_TITLE, title)
                }

                val intent = ChildActivity.makeFor(context!!, RichEditorFragment::class.java, args)
                startActivityForResult(intent, REQUEST_INFO)
            }
            R.id.readonly_container -> readOnlySwitch.toggle()
        }
    }

    private fun dbSave(): Boolean {
        val process: String?.() -> String? = {
            this?.trim()
                    ?.replace("\n", "")
                    ?.replace("\r", "")
        }

        subject.name = subject.name?.process()
        subject.description = subject.description?.trim()

        nameInputLayout.editText!!.setText(subject.name)

        // Client-side data rules
        if (subject.name.isNullOrBlank()) {
            return false
        }

        val firestore = FirebaseFirestore.getInstance()
        val doc = firestore.document("subjects/" + subject.id!!)

        if (!origin.id.isNullOrEmpty()) { // update subject
            val curr: MutableMap<String, Any?> = subject.deflateSubject()
            val prev: MutableMap<String, Any?> = origin.deflateSubject()

            // Update only changed entries
            val map = curr.filter { prev[it.key] != it.value }
            map.takeUnless { map.isEmpty() }?.also {
                Timber.tag(TAG).i("Subject UPDATE: [$map]")
                doc.update(it)
            }
        } else { // create subject
            val map = subject.deflateSubject().filter { it.value != null }
            Timber.tag(TAG).i("Subject CREATE: [$map]")

            firestore.batch().apply {
                set(doc, map)

                // Do the job of our trigger functions: add this subject to
                // user directly.
                val userId = subject.author!!
                set(
                        firestore.document("users/$userId/subjects/" + subject.id!!),
                        HashMap<String, Any?>().apply {
                            put("name", subject.name)
                            put("color", subject.color)
                            put("enabled", true)
                        }
                )
            }.commit()
        }

        return true
    }

    private fun dbRemove(): Boolean {
        if (origin.id.isNullOrBlank()) {
            return false
        }

        /*
        val rootRef = FirebaseDatabase.getInstance().reference
        rootRef.child("subjects").child(subject.id!!).setValue(null)
        */
        return true
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_palette -> showPaletteDialog()
            R.id.action_delete -> {
                val md = MaterialDialog.Builder(context!!)
                        .content(R.string.subject_remove_q)
                        .positiveText(R.string.action_remove)
                        .negativeText(android.R.string.cancel)
                        .onPositive { _, _ ->
                            val removed = dbRemove()
                            if (removed.not()) {
                                return@onPositive
                            }

                            // Finish the fragment
                            val host = activity as FragmentHost
                            host.fragmentFinish(this@SubjectEditFragment)
                        }
                        .build()

                md.getActionButton(DialogAction.POSITIVE).setTextColor(Palette.RED)
                md.show()
            }
            else -> return false
        }
        return true
    }

    override fun onColorSelected(@ColorInt color: Int) {
        colorTemp = color
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) when (requestCode) {
            REQUEST_INFO -> {
                if (resultCode == RichEditorFragment.RC_SAVED) {
                    val html = data.getStringExtra(RichEditorFragment.EXTRA_HTML)
                    setDescription(html)
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        updateCurrentSubject()
        outState.putParcelable(STATE_SUBJECT, subject)
    }

    private fun updateCurrentSubject() {
        subject.name = nameInputLayout.editText!!.text.toString()
        subject.archived = readOnlySwitch.isChecked
    }

    // --------------------------
    // -- DIALOGS ---------------
    // --------------------------

    private fun showPaletteDialog() {
        // Load icon
        val a = activity!!.theme.obtainStyledAttributes(intArrayOf(R.attr.icon_palette))
        val iconDrawableRes = a.getResourceId(0, 0)
        a.recycle()

        val md = MaterialDialog.Builder(context!!)
                .customView(R.layout.___dialog_palette, true)
                .iconRes(iconDrawableRes)
                .title(R.string.dialog_subject_palette)
                .negativeText(android.R.string.cancel)
                .positiveText(android.R.string.ok)
                .onAny { dialog, which ->
                    if (which == DialogAction.POSITIVE) setColor(colorTemp)
                    dialog.dismiss()
                }
                .autoDismiss(false)
                .build()

        val view = md.customView!!

        val paletteView = view.findViewById<SpectrumPalette>(R.id.palette)
        paletteView.setOnColorSelectedListener(this)
        paletteView.setFixedColumnCount(PALETTE.size)
        paletteView.setColors(PALETTE)

        val color = subject.color
        if (color != 0) {
            colorTemp = Palette.findColorByHue(PALETTE, color)
            paletteView.setSelectedColor(colorTemp)

            // Automatically scroll palette view
            // to ensure that selected item is shown on start
            paletteView.post {
                val p = PALETTE
                val width = paletteView.measuredWidth / p.size
                val pos = p.indices.firstOrNull { colorTemp == p[it] } ?: 0

                val scrollView = paletteView.parent as HorizontalScrollView
                scrollView.scrollBy(width * pos, 0)
            }
        }

        md.show()
    }

    // --------------------------
    // -- UNDO ------------------
    // --------------------------

    private fun setDescription(text: String?) {
        val message = getString(R.string.snackbar_description_changed)
        setValueWithUndo({ it.description }, { performSetDescription(it) }, text, message)
    }

    private fun performSetDescription(text: String?) {
        subject.description = text
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

    private fun setColor(@ColorInt color: Int) {
        val message = getString(R.string.snackbar_color_changed)
        setValueWithUndo({ it.color }, { performSetColor(it) }, color, message)
    }

    private fun performSetColor(@ColorInt color: Int) {
        subject.color = color

        appBar.setBackgroundColor(color)
        UiHelper.updateTextInputLayout(nameInputLayout, color)
        UiHelper.updateToolbarTitle(toolBar, color)
        UiHelper.updateToolbarBackIcon(toolBar, color)
        UiHelper.updateToolbarMenuIcons(toolBar, color, R.id.action_palette)
    }

    private fun <T> setValueWithUndo(get: (Subject) -> T, set: (T) -> Unit, value: T, text: String) {
        val previous = get.invoke(subject)
        set.invoke(value)
        Snackbar.make(appBar, text, Snackbar.LENGTH_LONG)
                .setAction(R.string.snackbar_undo) { set.invoke(previous) }
                .show()
    }

    // --------------------------
    // -- PROMPTS ---------------
    // --------------------------

    /**
     * If first time, shows a prompt about setting
     * the accent color of the subject.
     */
    @SuppressLint("RestrictedApi")
    private fun promptPaletteMenuItem() {
        if (Config[Config.KEY_PROMPT_SUBJECT_EDIT_PALETTE] && !Binfo.DEBUG_PROMPTS) {
            return
        }

        val view = toolBar.findMenuItemView(R.id.action_palette) ?: return
        MaterialTapTargetPrompt.Builder(activity!!)
                .setTarget(view)
                .setFocalColour(Color.TRANSPARENT)
                .setPrimaryText(getString(R.string.prompt_subject_edit_palette_title))
                .setSecondaryText(getString(R.string.prompt_subject_edit_palette_summary))
                .setPromptStateChangeListener { _, state ->
                    when (state) {
                        MaterialTapTargetPrompt.STATE_DISMISSED -> {
                            Config.edit(context!!)
                                    .put(Config.KEY_PROMPT_SUBJECT_EDIT_PALETTE, true)
                                    .commit()
                        }
                    }
                }
                .show()

    }
}
