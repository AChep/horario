package com.artemchep.horario.ui.fragments

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.design.widget.AppBarLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.app.NavUtils
import android.support.v7.widget.Toolbar
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.TextView
import com.artemchep.horario.Palette
import com.artemchep.horario.R
import com.artemchep.horario._new.BackHelper
import com.artemchep.horario._new.UiHelper
import com.artemchep.horario._new.activities.SubjectActivity
import com.artemchep.horario.database.data.Note
import com.artemchep.horario.database.data.Post
import com.artemchep.horario.database.generateKey
import com.artemchep.horario.extensions.withOnClick
import com.artemchep.horario.interfaces.FragmentHost
import com.artemchep.horario.ui.activities.ChildActivity
import com.artemchep.horario.ui.drawables.MenuIconDrawable
import com.artemchep.horario.ui.utils.EXTRA_COLOR
import com.artemchep.horario.ui.utils.EXTRA_POST
import com.artemchep.horario.ui.utils.EXTRA_SUBJECT_ID
import com.artemchep.horario.ui.utils.EXTRA_USER_ID
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.thebluealliance.spectrum.internal.ColorUtil
import es.dmoral.toasty.Toasty
import org.sufficientlysecure.htmltextview.HtmlTextView
import timber.log.Timber

/**
 * @author Artem Chepurnoy
 */
class SubjectPostEditFragment : FragmentBase(), Toolbar.OnMenuItemClickListener, View.OnClickListener {

    companion object {
        private val TAG = "SubjectPostEditFragment"

        private val STATE_POST = "state::post"

        private val REQUEST_INFO = 298
    }

    data class ThemeColors(
            val textPrimary: Int,
            val textHint: Int
    )

    private var color: Int = 0
    private lateinit var userId: String
    private lateinit var subjectId: String

    /**
     * Post passed as parameter to this class. Used
     * to determinate changes.
     */
    private lateinit var origin: Post
    private lateinit var post: Post

    private lateinit var dataRules: Array<(Post) -> String?>

    private lateinit var styles: ThemeColors

    private lateinit var fab: FloatingActionButton
    private lateinit var appBar: AppBarLayout
    private lateinit var toolBar: Toolbar
    private lateinit var titleEditView: EditText
    private lateinit var textHtmlView: HtmlTextView
    private lateinit var typeTextView: TextView
    private lateinit var typeIconView: View

    private val mBackHelper = BackHelper()

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        arguments!!.run {
            userId = getString(EXTRA_USER_ID)
            subjectId = getString(EXTRA_SUBJECT_ID)
            color = retrieveColor()
        }

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
        val s: Post? = arguments!!.getParcelable(EXTRA_POST)
        origin = s?.copy() ?: Post()
        post = if (savedState != null) {
            savedState.getParcelable(STATE_POST)
        } else {
            // Copy everything except author and generate
            // key if empty
            origin.copy(author = userId).apply {
                if (id.isNullOrEmpty()) id = generateKey()
            }
        }

        // Setup client-side rules. Those will be shown
        // after trying to save invalid data.
        dataRules = arrayOf( // TODO: Localize validation warning
                { post ->
                    if (post.text.isNullOrBlank()) {
                        "Content text should not be empty"
                    } else null
                }
        )
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.___fragment_subject_post__edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.attach).setOnClickListener {
            Toasty.normal(context!!, "Coming soon!").show()
        }

        fab = view.findViewById<FloatingActionButton>(R.id.fab).apply {
            setOnClickListener(this@SubjectPostEditFragment)
            // Update the icon
            setImageResource(if (origin.id.isNullOrEmpty()) {
                R.drawable.ic_publish_white_24dp
            } else R.drawable.ic_content_save_white_24dp)
        }
        appBar = view.findViewById(R.id.appbar)
        toolBar = appBar.findViewById<Toolbar>(R.id.toolbar).apply {
            setOnMenuItemClickListener(this@SubjectPostEditFragment)
            setNavigationOnClickListener(View.OnClickListener {
                origin.id?.takeIf { it.isNotBlank() }?.let {
                    val args = Bundle().apply {
                        putString(EXTRA_USER_ID, userId)
                        putString(EXTRA_SUBJECT_ID, subjectId)
                    }

                    // Navigate back to the subject activity
                    val intent = Intent(activity, SubjectActivity::class.java)
                    intent.putExtras(args)
                    NavUtils.navigateUpTo(activity!!, intent)
                    return@OnClickListener
                }

                val args = Bundle().apply {
                    putInt(EXTRA_COLOR, color)
                    putString(EXTRA_USER_ID, userId)
                    putString(EXTRA_SUBJECT_ID, subjectId)
                    putParcelable(EXTRA_POST, post)
                }

                // Navigate back to the post activity
                val intent = ChildActivity.makeFor(context, SubjectPostFragment::class.java, args)
                NavUtils.navigateUpTo(activity!!, intent)
            })
        }

        textHtmlView = view.findViewById<HtmlTextView>(R.id.text).withOnClick(this)
        titleEditView = view.findViewById<EditText>(R.id.title).apply {
            setText(post.title)
        }

        view.findViewById<View>(R.id.type).withOnClick(this).apply {
            typeTextView = findViewById(R.id.type_text)
            typeIconView = findViewById<View>(R.id.type_icon).apply {
                background = MenuIconDrawable()
            }
        }

        setupColorAccent(color)

        setType(post.type)

        performSetText(post.text)
    }

    private fun setupColorAccent(@ColorInt color: Int) {
        appBar.setBackgroundColor(color)
        UiHelper.updateToolbarTitle(toolBar, color)
        UiHelper.updateToolbarBackIcon(toolBar, color)

        val dark = ColorUtil.isColorDark(color)
        val primary = if (dark) Color.WHITE else Color.BLACK
        typeTextView.setTextColor(primary)
        val icon = typeIconView.background as MenuIconDrawable
        icon.setColor(primary)
    }

    private fun setType(type: Int) {
        post.type = type
        typeTextView.text = when (type) {
            Post.TYPE_POST -> getString(R.string.post)
            Post.TYPE_ANNOUNCEMENT -> getString(R.string.announcement)
            Post.TYPE_QUESTION -> getString(R.string.question)
            else -> "Unknown"
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.fab -> {
                updateCurrentPost()

                val saved = dbSave()
                if (saved.not()) {
                    return
                }

                // Finish the fragment
                val host = activity as FragmentHost
                host.fragmentFinish(this@SubjectPostEditFragment)
            }
            R.id.type -> {
                val icon = typeIconView.background as MenuIconDrawable
                icon.transformToUpsideDown()

                // Form popup menu and show it
                val popup = PopupMenu(activity, view)
                var i = 0
                for (d in arrayOf(
                        getString(R.string.post),
                        getString(R.string.announcement),
                        getString(R.string.question)
                )) popup.menu.add(0, i++, 0, d)
                popup.setOnMenuItemClickListener { item ->
                    setType(item.itemId)
                    false
                }
                popup.setOnDismissListener { icon.transformToNormal() }
                popup.show()
            }
            R.id.text -> {
                val args = Bundle().apply {
                    val title = getString(R.string.__subject_post_text)
                    putString(RichEditorFragment.EXTRA_HTML, post.text)
                    putString(RichEditorFragment.EXTRA_TITLE, title)
                }

                val intent = ChildActivity.makeFor(context!!, RichEditorFragment::class.java, args)
                startActivityForResult(intent, REQUEST_INFO)
            }
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) when (requestCode) {
            REQUEST_INFO -> {
                if (resultCode == RichEditorFragment.RC_SAVED) {
                    val text = data.getStringExtra(RichEditorFragment.EXTRA_HTML)
                    setText(text)
                }
            }
        }
    }

    private fun updateCurrentPost() {
        post.title = titleEditView.text.toString()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        updateCurrentPost()
        outState.putParcelable(STATE_POST, post)
    }

    override fun onBackPressed(): Boolean = mBackHelper.onBackPressed(context!!)

    /**
     * Saves the post or creates a new one if it has no [Post.getKey]  key}.
     *
     * @return `true` if saving succeed,
     * `false` otherwise
     */
    private fun dbSave(): Boolean {
        val process: String?.() -> String? = {
            this?.trim()
                    ?.replace("\n", "")
                    ?.replace("\r", "")
        }

        post.title = post.title?.process()
        post.text = post.text?.trim()

        titleEditView.setText(post.title)

        // Client-side data rules
        var v = false
        val sb = StringBuilder()
        dataRules.forEach {
            val msg = it.invoke(post)
            if (msg != null) {
                if (v) {
                    sb.append('\n')
                } else v = true
                sb.append("• $msg")
            }
        }
        if (v) {
            Toasty.warning(context!!, sb).show()
            return false
        }

        val firestore = FirebaseFirestore.getInstance()
        val doc = firestore.document("subjects/$subjectId/posts/" + post.id!!)

        if (!origin.id.isNullOrEmpty()) { // update post
            val curr = post.deflatePost()
            val prev = origin.deflatePost()

            // Update only changed entries
            val map = curr.filter { prev[it.key] != it.value }
            map.takeUnless { map.isEmpty() }?.also {
                Timber.tag(TAG).i("Post UPDATE: [$map]")
                doc.update(it)
            }
        } else { // create post
            val map = post.deflatePost().apply {
                put("timestamp", FieldValue.serverTimestamp())
            }.filter { it.value != null }
            Timber.tag(TAG).i("Post CREATE: [$map]")
            doc.set(map)
        }

        return true
    }

    // --------------------------
    // -- UNDO ------------------
    // --------------------------


    private fun setText(text: String?) {
        val message = getString(R.string.snackbar_description_changed)
        setValueWithUndo({ it.text }, { performSetText(it) }, text, message)
    }

    private fun performSetText(text: String?) {
        post.text = text
        fab.run {
            if (text.isNullOrBlank()) {
                hide()
            } else show()
        }
        textHtmlView.apply {
            if (text.isNullOrBlank()) {
                setText(R.string.__subject_post_text)
                setTextColor(styles.textHint)
            } else {
                setHtml(text!!)
                setTextColor(styles.textPrimary)
                movementMethod = null // clickable
            }
        }.withOnClick(this)
    }

    private fun <T> setValueWithUndo(get: (Post) -> T, set: (T) -> Unit, value: T, text: String) {
        val previous = get.invoke(post)
        set.invoke(value)
        Snackbar.make(appBar, text, Snackbar.LENGTH_LONG)
                .setAction(R.string.snackbar_undo) { set.invoke(previous) }
                .show()
    }

}
