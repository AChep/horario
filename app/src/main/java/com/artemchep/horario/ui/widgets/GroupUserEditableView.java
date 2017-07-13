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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.artemchep.horario.R;

/**
 * @author Artem Chepurnoy
 */
public class GroupUserEditableView extends FrameLayout implements View.OnClickListener {


    private enum State {

    }

    private GroupUserView mGroupUserView;
    private ImageButton mDeleteButton;
    private ImageButton mApplyButton;
    private TextInputEditText mEditTextName;
    private TextInputLayout mTextInputName;

    private OnActionButtonClickListener mActionButtonListener;

    /**
     * @author Artem Chepurnoy
     */
    public enum ActionButton {
        DELETE,
    }

    /**
     * @author Artem Chepurnoy
     */
    public interface OnActionButtonClickListener {

        void onClick(@NonNull View view, @Nullable String uid, @NonNull ActionButton button);

    }

    public GroupUserEditableView(Context context) {
        super(context);
    }

    public GroupUserEditableView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mGroupUserView = (GroupUserView) findViewById(R.id.user);
        mDeleteButton = (ImageButton) findViewById(R.id.delete);
        mApplyButton = (ImageButton) findViewById(R.id.apply);
        mEditTextName = (TextInputEditText) findViewById(R.id.input_uid);
        mTextInputName = (TextInputLayout) findViewById(R.id.input_layout_uid);

        mGroupUserView.setOnClickListener(this);
        mDeleteButton.setOnClickListener(this);
        mApplyButton.setOnClickListener(this);

        twoColumnsWithDetails();
    }

    @Override
    public void onClick(View v) {
        if (v == mGroupUserView) {
            twoColumnsWithDetails();
            mEditTextName.requestFocus();
        } else if (v == mDeleteButton) {
            if (mActionButtonListener != null) {
                mActionButtonListener.onClick(v, getUser(), ActionButton.DELETE);
            }
        } else if (v == mApplyButton) {
            mGroupUserView.setUser(getUser());
            onwColumnsWithDetails();
        }
    }

    public void setOnActionButtonClickListener(OnActionButtonClickListener listener) {
        mActionButtonListener = listener;
    }

    public void setUser(@NonNull String uid) {
        mEditTextName.setText(uid);
        mGroupUserView.setUser(uid);
    }

    @NonNull
    public String getUser() {
        return mEditTextName.getText().toString();
    }

    public void twoColumnsWithDetails() {
        mGroupUserView.setVisibility(INVISIBLE);
        mDeleteButton.setVisibility(INVISIBLE);
        mApplyButton.setVisibility(VISIBLE);
        mTextInputName.setVisibility(VISIBLE);
    }

    public void onwColumnsWithDetails() {
        mGroupUserView.setVisibility(VISIBLE);
        mDeleteButton.setVisibility(VISIBLE);
        mApplyButton.setVisibility(INVISIBLE);
        mTextInputName.setVisibility(GONE);
    }

}
