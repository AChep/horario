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
package com.artemchep.horario.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.support.v4.app.DialogFragment
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.HorizontalScrollView
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.artemchep.horario.Palette
import com.artemchep.horario.Palette.PALETTE
import com.artemchep.horario.R
import com.artemchep.horario.database.data.Subject
import com.artemchep.horario.database.models.ISubject
import com.artemchep.horario.ui.utils.EXTRA_SUBJECT
import com.artemchep.horario.ui.utils.EXTRA_USER_ID
import com.google.firebase.firestore.FirebaseFirestore
import com.thebluealliance.spectrum.SpectrumPalette
import timber.log.Timber

/**
 * Dialog for editing user subjects; mainly only
 * [subject][Subject].
 *
 * @author Artem Chepurnoy
 * @see .EXTRA_USER_ID
 * @see .EXTRA_SUBJECT
 */
class SubjectDialog : DialogFragment(), SpectrumPalette.OnColorSelectedListener {

    companion object {
        private const val TAG = "SubjectDialog"
        private const val STATE_SUBJECT = "state::subject"
    }

    private lateinit var firestore: FirebaseFirestore
    private lateinit var userId: String
    private lateinit var subject: Subject

    private lateinit var nameEditText: TextInputEditText
    private lateinit var nameInputLayout: TextInputLayout

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        firestore = FirebaseFirestore.getInstance()
    }

    override fun onCreateDialog(savedState: Bundle?): Dialog {
        userId = arguments!!.getString(EXTRA_USER_ID)
        subject = if (savedState != null) {
            savedState.getParcelable(STATE_SUBJECT)
        } else {
            val s: ISubject = arguments!!.getParcelable(EXTRA_SUBJECT)
            Subject(
                    id = s.id,
                    name = s.name,
                    color = s.color
            )
        }

        // Load icon
        val a = activity!!.theme.obtainStyledAttributes(intArrayOf(R.attr.icon_label))
        val iconDrawableRes = a.getResourceId(0, 0)
        a.recycle()

        val md = MaterialDialog.Builder(activity!!)
                .customView(R.layout.___dialog_subject__local, true)
                .title("Edit subject locally")
                .negativeText(android.R.string.cancel)
                .positiveText(R.string.dialog_save)
                .iconRes(iconDrawableRes)
                .onAny(MaterialDialog.SingleButtonCallback { _, which ->
                    if (which == DialogAction.POSITIVE) {
                        updateCurrentSubject()

                        val saved = dbSave()
                        if (!saved) return@SingleButtonCallback
                    }
                    dismiss()
                })
                .autoDismiss(false)
                .build()

        val view = md.customView!!
        nameInputLayout = view.findViewById(R.id.subject_name_input_layout)
        nameEditText = view.findViewById(R.id.subject_name)
        nameEditText.setText(subject.name)
        nameEditText.addTextChangedListener(Watcher(nameEditText))

        val paletteView = view.findViewById<SpectrumPalette>(R.id.palette)
        paletteView.setOnColorSelectedListener(this)
        paletteView.setFixedColumnCount(PALETTE.size)
        paletteView.setColors(PALETTE)
        paletteView.setSelectedColor(Palette.findColorByHue(PALETTE, subject.color))

        // Automatically scroll palette view
        // to ensure that selected item is shown on start
        paletteView.post {
            val p = PALETTE
            val w = paletteView.measuredWidth / p.size
            val pos = p.indices.firstOrNull { subject.color == p[it] } ?: 0

            val scrollView = paletteView.parent as HorizontalScrollView
            scrollView.scrollBy(w * pos, 0)
        }

        return md
    }

    override fun onColorSelected(@ColorInt color: Int) {
        subject.color = color
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        updateCurrentSubject()
        outState.putParcelable(STATE_SUBJECT, subject)
    }

    private fun dbSave(): Boolean {
        val process: String?.() -> String? = {
            this?.trim()
                    ?.replace("\n", "")
                    ?.replace("\r", "")
        }

        subject.name = subject.name?.process()
        nameInputLayout.editText!!.setText(subject.name)

        // Client-side data rules
        if (subject.name.isNullOrBlank()) {
            return false
        }

        val firestore = FirebaseFirestore.getInstance()
        val doc = firestore.document("users/$userId/subjects/" + subject.id!!)
        val map = subject.deflateSubject().filter { it.value != null }

        Timber.tag(TAG).i("SubjectLocal CREATE: [$map]")
        doc.set(map)
        return true
    }

    private fun updateCurrentSubject() {
        subject.name = name
    }

    private fun validateName(): Boolean {
        if (TextUtils.isEmpty(name)) {
            val errorMsg = getString(R.string.dialog_subject_error_enter_name)
            nameEditText.requestFocus()
            nameInputLayout.error = errorMsg
            return false
        }

        nameInputLayout.isErrorEnabled = false
        return true
    }

    /**
     * @return the name of the subject
     * @see Subject.name
     */
    private val name: String
        get() = nameEditText.text.toString().trim { it <= ' ' }

    /**
     * @author Artem Chepurnoy
     */
    private inner class Watcher constructor(view: View) : TextWatcher {

        private val viewId: Int = view.id

        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

        override fun afterTextChanged(editable: Editable) {
            when (viewId) {
                R.id.subject_name -> validateName()
            }
        }
    }

}
