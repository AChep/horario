/*
 * Copyright (C) 2017 XJSHQ@github.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package com.artemchep.horario.ui.activities

import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.artemchep.horario.Config
import com.artemchep.horario.Palette
import com.artemchep.horario.R
import com.artemchep.horario._new.BackHelper
import com.artemchep.horario._new.activities.MultiPaneActivity
import com.artemchep.horario.database.data.Note
import com.artemchep.horario.database.generateKey
import com.artemchep.horario.database.models.INote
import com.artemchep.horario.extensions.transitionNameCompat
import com.artemchep.horario.extensions.withOnClick
import com.artemchep.horario.ui.utils.EXTRA_NOTE
import com.artemchep.horario.ui.utils.EXTRA_USER_ID
import com.github.nitrico.fontbinder.FontBinder
import com.google.firebase.firestore.FirebaseFirestore
import es.dmoral.toasty.Toasty
import timber.log.Timber
import java.util.*


/**
 * @author Artem Chepurnoy
 */
class NoteActivity : MultiPaneActivity(), Toolbar.OnMenuItemClickListener, View.OnClickListener {

    companion object {
        const val TAG = "NoteActivity"
        const val EXTRA_TRANSITION = "extra::transition"
        const val EXTRA_TRANSITION_TEXT_NAME = "extra::transition::text"
        const val EXTRA_TRANSITION_CARD_NAME = "extra::transition::card"
        const val STATE_NOTE = "state::note"
    }

    private lateinit var userId: String
    private lateinit var origin: Note
    private lateinit var note: Note

    private lateinit var dataRules: Array<(Note) -> String?>

    private lateinit var toolbar: Toolbar
    private lateinit var saveButton: View
    private lateinit var textEditView: EditText

    private val mBackHelper = BackHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupTheme()

        intent.extras.run {
            userId = getString(EXTRA_USER_ID)
        }

        val s: INote? = intent.extras.getParcelable(EXTRA_NOTE)
        origin = s?.let {
            Note(id = it.id).apply {
                text = it.text
            }
        } ?: Note()
        note = if (savedInstanceState != null) {
            savedInstanceState.getParcelable(STATE_NOTE)
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

        setContentView(R.layout.activity_note)

        toolbar = findViewById<Toolbar>(R.id.toolbar).apply {
            inflateMenu(R.menu.___note__edit)
            setOnMenuItemClickListener(this@NoteActivity)

            menu.findItem(R.id.action_delete).apply {
                isVisible = !origin.id.isNullOrEmpty()
            }
        }

        saveButton = findViewById<View>(R.id.save).withOnClick(this)
        textEditView = findViewById<EditText>(R.id.text).apply {
            typeface = FontBinder["RobotoSlab-Regular"]
            setText(note.text)
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }
            })
        }

        intent.extras.takeIf { it.getBoolean(EXTRA_TRANSITION, false) }?.run {
            val cardView = findViewById<View>(R.id.card)
            cardView.transitionNameCompat = getString(EXTRA_TRANSITION_CARD_NAME)
        }
    }

    protected fun setupTheme() {
        val theme = Config.get<Int>(Config.KEY_UI_THEME)
        when (theme) {
            Config.THEME_LIGHT -> setTheme(R.style.AppThemeLight_NoActionBar)
            Config.THEME_DARK -> setTheme(R.style.AppTheme_NoActionBar)
            Config.THEME_BLACK -> setTheme(R.style.AppThemeBlack_NoActionBar)
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete -> {
                val md = MaterialDialog.Builder(this)
                        .content(R.string.note_remove_q)
                        .positiveText(R.string.action_remove)
                        .negativeText(android.R.string.cancel)
                        .onPositive { _, _ ->
                            val deleted = dbDelete()
                            if (deleted.not()) {
                                return@onPositive
                            }

                            // Finish the activity
                            supportFinishAfterTransition()
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

    override fun onClick(view: View) {
        when (view.id) {
            R.id.save -> {
                updateCurrentNote()

                val saved = dbSave()
                if (saved.not()) {
                    return
                }

                // Finish the activity
                supportFinishAfterTransition()
            }
        }
    }

    private fun updateCurrentNote() {
        note.text = textEditView.text.toString()
    }

    private fun dbDelete(): Boolean {
        if (origin.id.isNullOrEmpty()) {
            return false
        }

        val firestore = FirebaseFirestore.getInstance()
        val doc = firestore.document("users/$userId/notes/" + note.id!!)
        doc.delete()
        return true
    }

    /**
     * Saves the note or creates a new one.
     *
     * @return `true` if saving succeed,
     * `false` otherwise
     */
    private fun dbSave(): Boolean {
        note.text = note.text?.trim()
        textEditView.setText(note.text)

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
            Toasty.warning(this, sb).show()
            return false
        }

        val firestore = FirebaseFirestore.getInstance()
        val doc = firestore.document("users/$userId/notes/" + note.id!!)

        if (!origin.id.isNullOrEmpty()) { // update post
            val curr = note.deflateNote()
            val prev = origin.deflateNote()

            // Update only changed entries
            val map = curr.filter { prev[it.key] != it.value }
            map.takeUnless { map.isEmpty() }?.also {
                Timber.tag(TAG).i("Note UPDATE: [$map]")
                doc.update(it)
            }
        } else { // create post
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

            Timber.tag(TAG).i("Note CREATE: [$map]")
            doc.set(map)
        }

        return true
    }

    private fun hasChanged(): Boolean {
        updateCurrentNote()
        return origin.text != note.text || origin.date != note.date
    }

    override fun onBackPressed() {
        if (hasChanged()) {
            val eaten = mBackHelper.onBackPressed(this)
            if (eaten) return
        }

        super.onBackPressed()
    }

}
