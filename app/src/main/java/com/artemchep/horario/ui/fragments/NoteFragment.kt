package com.artemchep.horario.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.NavUtils
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.artemchep.horario.Palette
import com.artemchep.horario.R
import com.artemchep.horario._new.BackHelper
import com.artemchep.horario._new.activities.MainActivity
import com.artemchep.horario.database.data.Note
import com.artemchep.horario.database.generateKey
import com.artemchep.horario.interfaces.FragmentHost
import com.artemchep.horario.ui.activities.NoteActivity
import com.artemchep.horario.ui.utils.EXTRA_NOTE
import com.artemchep.horario.ui.utils.EXTRA_USER_ID
import com.github.nitrico.fontbinder.FontBinder
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import es.dmoral.toasty.Toasty
import timber.log.Timber
import java.util.*

/**
 * @author Artem Chepurnoy
 */
class NoteFragment : FragmentBase(), Toolbar.OnMenuItemClickListener, View.OnClickListener {

    companion object {
        private val TAG = "NoteFragment"

        private val STATE_NOTE = "state::note"
    }

    private lateinit var userId: String

    /**
     * Note passed as parameter to this class. Used
     * to determinate changes.
     */
    private lateinit var origin: Note
    private lateinit var note: Note

    private lateinit var dataRules: Array<(Note) -> String?>

    private lateinit var fab: FloatingActionButton
    private lateinit var appBar: AppBarLayout
    private lateinit var toolBar: Toolbar
    private lateinit var textEditText: EditText

    private val backHelper = BackHelper()

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        arguments!!.run {
            userId = getString(EXTRA_USER_ID)
        }
    }

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        val s: Note? = arguments!!.getParcelable(EXTRA_NOTE)
        origin = s?.copy() ?: Note()
        note = if (savedState != null) {
            savedState.getParcelable(STATE_NOTE)
        } else {
            // Copy everything except author and generate
            // key if empty
            origin.copy().apply {
                if (id.isNullOrEmpty()) id = generateKey()
            }
        }

        // Setup client-side rules. Those will be shown
        // after trying to save invalid data.
        dataRules = arrayOf( // TODO: Localize validation warning
                { note ->
                    if (note.text.isNullOrBlank()) {
                        "Content text should not be empty"
                    } else null
                }
        )
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.___fragment_note, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fab = view.findViewById<FloatingActionButton>(R.id.fab).apply {
            setOnClickListener(this@NoteFragment)
            // Update the icon
            setImageResource(if (origin.id.isNullOrEmpty()) {
                R.drawable.ic_publish_white_24dp
            } else R.drawable.ic_content_save_white_24dp)
        }
        appBar = view.findViewById(R.id.appbar)
        toolBar = appBar.findViewById<Toolbar>(R.id.toolbar).apply {
            setNavigationOnClickListener({
                val intent = Intent(activity, MainActivity::class.java)
                intent.addCategory(MainActivity.CATEGORY_SUBJECTS)
                NavUtils.navigateUpTo(activity!!, intent)
            })
            // Setup menu
            inflateMenu(R.menu.___note__edit)
            setOnMenuItemClickListener(this@NoteFragment)

            menu.findItem(R.id.action_delete).apply {
                isVisible = !origin.id.isNullOrEmpty()
            }
        }

        textEditText = view.findViewById<EditText>(R.id.text).apply {
            typeface = FontBinder["RobotoSlab-Regular"]
            setText(note.text)
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.fab -> {
                updateCurrentNote()

                val saved = dbSave()
                if (saved.not()) {
                    return
                }

                // Finish the fragment
                val host = activity as FragmentHost
                host.fragmentFinish(this@NoteFragment)
            }
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete -> {
                val md = MaterialDialog.Builder(context!!)
                        .content(R.string.note_remove_q)
                        .positiveText(R.string.action_remove)
                        .negativeText(android.R.string.cancel)
                        .onPositive { _, _ ->
                            val deleted = dbDelete()
                            if (deleted.not()) {
                                return@onPositive
                            }

                            // Finish the fragment
                            val host = activity as FragmentHost
                            host.fragmentFinish(this@NoteFragment)
                        }
                        .build()

                md.getActionButton(DialogAction.POSITIVE).setTextColor(Palette.RED)
                md.show()
            }
            R.id.action_remind -> {
            }
            R.id.action_list -> {
            }
            else -> return false
        }
        return true
    }

    private fun updateCurrentNote() {
        note.text = textEditText.text.toString()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        updateCurrentNote()
        outState.putParcelable(STATE_NOTE, note)
    }

    override fun onBackPressed(): Boolean = backHelper.onBackPressed(context!!)

    /**
     * @return `true` if note is valid for saving,
     * `false` otherwise
     */
    private fun dbSave(): Boolean {
        note.text = note.text?.trim()
        textEditText.setText(note.text)

        // Client-side data rules
        var v = false
        val sb = StringBuilder()
        dataRules.forEach {
            val msg = it.invoke(note)
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
        val doc = firestore.document("users/$userId/notes/" + note.id!!)

        if (!origin.id.isNullOrEmpty()) { // update note
            val curr = note.deflateNote()
            val prev = origin.deflateNote()

            // Update only changed entries
            val map = curr.filter { prev[it.key] != it.value }
            map.takeUnless { map.isEmpty() }?.also {
                Timber.tag(NoteActivity.TAG).i("Note UPDATE: [$map]")
                doc.update(it)
            }
        } else { // create note
            val map = note.deflateNote().apply {
                // Create priority text
                val now = System.currentTimeMillis()
                var priority = now.toString(Character.MAX_RADIX)
                val size = priority.length
                if (size < 14) {
                    val c = CharArray(14 - size)
                    Arrays.fill(c, '0')
                    priority = String(c) + priority
                }

                put("priority", priority)
            }.filter { it.value != null }

            Timber.tag(NoteActivity.TAG).i("Note CREATE: [$map]")
            doc.set(map)
        }

        return true
    }

    /**
     * @return `true` if note is valid for deleting,
     * `false` otherwise
     */
    private fun dbDelete(): Boolean {
        if (origin.id.isNullOrEmpty()) {
            return false
        }

        val firestore = FirebaseFirestore.getInstance()
        val doc = firestore.document("users/$userId/notes/" + note.id!!)
        doc.delete()
        return true
    }

}
