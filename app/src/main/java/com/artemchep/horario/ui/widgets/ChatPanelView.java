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
package com.artemchep.horario.ui.widgets;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.transition.TransitionManager;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.ViewUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.artemchep.horario.R;

/**
 * @author Artem Chepurnoy
 */
public abstract class ChatPanelView<T extends Parcelable> extends LinearLayout implements View.OnClickListener {

    private static final String STATE_MODE_TXT = "state::mode::message_text";
    private static final String STATE_MODE = "state::mode";
    private static final String STATE_OBJECT = "state::object";
    private static final String STATE_ORIGIN = "state::original";

    private boolean mInitialized;
    private boolean mAttachedToWindow;

    protected View mHeaderView;
    protected View mHeaderCloseView;
    protected TextView mHeaderTitleView;

    protected FrameLayout mContentView;

    protected EditText mWorkplaceEditText;
    protected ImageView mWorkplaceActionView;
    private boolean mIsShowingActionView = false;

    private Mode mMode = Mode.MESSAGE;
    private T mObject = null;

    private Callback<T> mCallback;

    private String mModeMessageText;

    /**
     * @author Artem Chepurnoy
     */
    public enum Mode {
        MESSAGE,
        REPLY,
        EDIT,
    }

    /**
     * @author Artem Chepurnoy
     */
    public interface Callback<T> {

        boolean onAction(ChatPanelView view, Mode mode, T object);

    }

    /**
     * @author Artem Chepurnoy
     */
    private class TxtWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            updateWorkspaceActionView();
        }

    }

    public ChatPanelView(Context context) {
        super(context);
        init();
    }

    public ChatPanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        if (mInitialized) {
            return;
        } else mInitialized = true;

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mContentView = findViewById(R.id.chat_content);
        mHeaderView = findViewById(R.id.chat_header);
        mHeaderTitleView = mHeaderView.findViewById(R.id.chat_header_title);
        mHeaderCloseView = mHeaderView.findViewById(R.id.chat_header_close);
        mHeaderCloseView.setOnClickListener(this);

        View workplace = findViewById(R.id.chat_workspace);
        mWorkplaceEditText = workplace.findViewById(R.id.chat_workspace_text);
        mWorkplaceEditText.addTextChangedListener(new TxtWatcher());
        mWorkplaceActionView = workplace.findViewById(R.id.chat_workspace_action);
        mWorkplaceActionView.setOnClickListener(this);

        setMode(mMode, mObject);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;

            // Restore root view's params
            Parcelable origin = bundle.getParcelable(STATE_ORIGIN);
            super.onRestoreInstanceState(origin);

            // Restore our params
            String mode = bundle.getString(STATE_MODE, Mode.MESSAGE.name());
            mMode = Mode.valueOf(mode);
            mModeMessageText = bundle.getString(STATE_MODE_TXT);
            mObject = bundle.getParcelable(STATE_OBJECT);

            setMode(mMode, mObject);
        }
        super.onRestoreInstanceState(state);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mAttachedToWindow = true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mAttachedToWindow = false;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(STATE_ORIGIN, super.onSaveInstanceState());
        bundle.putParcelable(STATE_OBJECT, mObject);
        bundle.putString(STATE_MODE, mMode.name());
        bundle.putString(STATE_MODE_TXT, mModeMessageText);
        return bundle;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            // Switch back to message mode on
            // closing contextual panel.
            case R.id.chat_header_close:
                setMode(Mode.MESSAGE, null);
                break;
            // Send/reply/edit message
            case R.id.chat_workspace_action: {
                if (mCallback != null) {
                    if (mCallback.onAction(this, mMode, mObject)) {
                        mWorkplaceEditText.setText(null);
                        setMode(Mode.MESSAGE, null);
                    }
                }
                break;
            }
        }
    }

    @NonNull
    public String getMessage() {
        return mWorkplaceEditText.getText().toString();
    }

    public void setCallback(@NonNull Callback<T> callback) {
        mCallback = callback;
    }

    public void setMode(@NonNull Mode mode, @Nullable T object) {
        if (mMode != mode && ViewCompat.isLaidOut(this)) {
            TransitionManager.beginDelayedTransition((ViewGroup) this.getParent());
        }

        if (mMode == Mode.MESSAGE) {
            // Save current message text, so when we back to
            // message mode we can restore it.
            mModeMessageText = getMessage();
        }

        mMode = mode;
        mObject = object;

        int actionIconRes;
        switch (mode) {
            case REPLY:
                actionIconRes = R.drawable.ic_reply_grey600_24dp;
                break;
            case EDIT:
                actionIconRes = R.drawable.ic_check_grey600_24dp;
                break;
            default:
            case MESSAGE:
                actionIconRes = R.drawable.ic_send_grey600_24dp;
                break;
        }

        if (mMode == Mode.MESSAGE) {
            mWorkplaceEditText.setText(mModeMessageText);
            mWorkplaceEditText.post(new Runnable() {
                @Override
                public void run() {
                    mWorkplaceEditText.setSelection(mWorkplaceEditText.length());
                }
            });
        } else mWorkplaceEditText.setText(null); // clear text
        mWorkplaceActionView.setImageResource(actionIconRes);
        updateWorkspaceActionView();

        mContentView.removeAllViews();
        switch (mode) {
            case REPLY: {
                mHeaderView.setVisibility(VISIBLE);
                mHeaderTitleView.setText(R.string.subject_stream_context_reply);

                // Setup content view
                View view = onCreateReplyView(mObject);
                mContentView.addView(view);
                break;
            }
            case EDIT: {
                mHeaderView.setVisibility(VISIBLE);
                mHeaderTitleView.setText(R.string.subject_stream_context_edit);

                // Setup content view
                View view = onCreateEditView(mObject);
                mContentView.addView(view);
                break;
            }
            default:
            case MESSAGE:
                mHeaderView.setVisibility(GONE);
                break;
        }
    }

    private void updateWorkspaceActionView() {
        boolean shown = mWorkplaceEditText.length() > 0;
        if (mIsShowingActionView == shown) {
            return;
        } else mIsShowingActionView = shown;

        if (shown) {
            mWorkplaceActionView.setClickable(true);
            mWorkplaceActionView.animate().alpha(1).scaleX(1f).scaleY(1f).setDuration(120);
        } else {
            mWorkplaceActionView.setClickable(false);
            mWorkplaceActionView.animate().alpha(0).scaleX(0.4f).scaleY(0.4f).setDuration(120);
        }
    }

    protected abstract View onCreateReplyView(@NonNull T object);

    protected abstract View onCreateEditView(@NonNull T object);

}
