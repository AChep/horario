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
package com.artemchep.horario.ui.widgets

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.artemchep.horario.R

/**
 * @author Artem Chepurnoy
 */
abstract class MessageBar<T : Parcelable> : LinearLayout, View.OnClickListener {

    companion object {
        private val STATE_MODE_TXT = "state::mode::message_text"
        private val STATE_MODE = "state::mode"
        private val STATE_OBJECT = "state::object"
        private val STATE_ORIGIN = "state::original"
    }

    /**
     * @author Artem Chepurnoy
     */
    enum class Mode {
        MESSAGE,
        REPLY,
        EDIT
    }

    /**
     * @author Artem Chepurnoy
     */
    interface Callback<T : Parcelable> {
        /**
         * @return `true` if action handled successfully, `false` otherwise.
         * In terms of UI `true` [sets][setMode] [message mode][Mode.MESSAGE]
         * and clears current [message].
         */
        fun onAction(view: MessageBar<T>, mode: Mode, obj: T?): Boolean
    }

    private var mIsShowingActionView = false

    var callback: Callback<T>? = null
    var mode = Mode.MESSAGE
        private set

    private var obj: T? = null

    val message: String
        get() = workplaceEditText.text.toString()
    private var messageSaved: String? = null

    private lateinit var headerView: View
    private lateinit var headerCloseView: View
    private lateinit var headerTitleView: TextView
    private lateinit var contentView: FrameLayout
    private lateinit var workplaceEditText: EditText
    private lateinit var workplaceActionView: ImageView

    /**
     * @author Artem Chepurnoy
     */
    private inner class TxtWatcher : TextWatcher {

        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

        override fun afterTextChanged(editable: Editable) {
            updateWorkspaceActionView()
        }

    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onFinishInflate() {
        super.onFinishInflate()

        contentView = findViewById(R.id.chat_content)
        headerView = findViewById<View>(R.id.chat_header).apply {
            headerTitleView = findViewById(R.id.chat_header_title)
            headerCloseView = findViewById<View>(R.id.chat_header_close).apply {
                setOnClickListener(this@MessageBar)
            }
        }

        val workplace = findViewById<View>(R.id.chat_workspace)
        workplaceEditText = workplace.findViewById(R.id.chat_workspace_text)
        workplaceEditText.addTextChangedListener(TxtWatcher())
        workplaceActionView = workplace.findViewById(R.id.chat_workspace_action)
        workplaceActionView.setOnClickListener(this)

        setMode(mode, obj)
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state is Bundle) {
            // Restore root view's params
            val origin = state.getParcelable<Parcelable>(STATE_ORIGIN)
            super.onRestoreInstanceState(origin)

            // Restore our params
            val mode = state.getString(STATE_MODE, Mode.MESSAGE.name)
            this.mode = Mode.valueOf(mode)
            messageSaved = state.getString(STATE_MODE_TXT)
            obj = state.getParcelable(STATE_OBJECT)

            setMode(this.mode, obj)
        } else super.onRestoreInstanceState(state)
    }

    override fun onSaveInstanceState(): Parcelable? {
        return Bundle().apply {
            putParcelable(STATE_ORIGIN, super.onSaveInstanceState())
            putParcelable(STATE_OBJECT, obj)
            putString(STATE_MODE, mode.name)
            putString(STATE_MODE_TXT, messageSaved)
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
        // Switch back to message mode on
        // closing contextual panel.
            R.id.chat_header_close -> setMode(Mode.MESSAGE, null)
        // Send/reply/edit message
            R.id.chat_workspace_action -> callback?.let {
                if (it.onAction(this, mode, obj)) {
                    workplaceEditText.text = null
                    setMode(Mode.MESSAGE, null)
                }
            }
        }
    }

    private fun updateWorkspaceActionView() {
        val shown = workplaceEditText.length() > 0
        if (mIsShowingActionView == shown) {
            return
        } else
            mIsShowingActionView = shown

        if (shown) {
            workplaceActionView.isClickable = true
            workplaceActionView.animate().alpha(1f).scaleX(1f).scaleY(1f).duration = 120
        } else {
            workplaceActionView.isClickable = false
            workplaceActionView.animate().alpha(0f).scaleX(0.4f).scaleY(0.4f).duration = 120
        }
    }

    protected abstract fun onCreateReplyView(vg: ViewGroup, obj: T): View

    protected abstract fun onExcractMessage(obj: T?): String?

    fun setMode(mode: Mode, obj: T?) {
        if (this.mode == Mode.MESSAGE) {
            // Save current message, so when we back to
            // message mode we can restore it.
            messageSaved = message
        }

        this.mode = mode
        this.obj = obj

        workplaceEditText.run {
            if (mode == Mode.MESSAGE) {
                setText(messageSaved) // restore message
                post { setSelection(length()) }
            } else setText(onExcractMessage(obj))
        }

        workplaceActionView.setImageResource(when (mode) {
            MessageBar.Mode.MESSAGE -> R.drawable.ic_send_grey600_24dp
            MessageBar.Mode.REPLY -> R.drawable.ic_reply_grey600_24dp
            MessageBar.Mode.EDIT -> R.drawable.ic_check_grey600_24dp
        })

        updateWorkspaceActionView()

        contentView.removeAllViews()
        headerView.visibility = when (mode) {
            MessageBar.Mode.REPLY -> {
                headerTitleView.setText(R.string.subject_stream_context_reply)

                // Setup content view
                val view = onCreateReplyView(contentView, obj!!)
                contentView.addView(view)

                View.VISIBLE
            }
            MessageBar.Mode.EDIT -> {
                headerTitleView.setText(R.string.subject_stream_context_edit)

                // Setup content view
                val view = onCreateEditView(contentView, obj!!)
                contentView.addView(view)

                View.VISIBLE
            }
            MessageBar.Mode.MESSAGE -> View.GONE
        }
    }

    protected abstract fun onCreateEditView(vg: ViewGroup, obj: T): View

}
