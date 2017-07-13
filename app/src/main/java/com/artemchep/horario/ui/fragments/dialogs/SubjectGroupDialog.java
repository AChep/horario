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
package com.artemchep.horario.ui.fragments.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.artemchep.horario.R;
import com.artemchep.horario.database.Db;
import com.artemchep.horario.database.models.SubjectGroup;
import com.artemchep.horario.models.Subject;
import com.artemchep.horario.ui.fragments.dialogs.base.DialogFragment;
import com.artemchep.horario.ui.widgets.GroupUserEditableView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

/**
 * @author Artem Chepurnoy
 */
public class SubjectGroupDialog extends DialogFragment implements
        GroupUserEditableView.OnActionButtonClickListener,
        View.OnClickListener {

    public static final String EXTRA_PATH = "extra::path";
    public static final String EXTRA_GROUP = "group";

    private String mTimetablePath;
    private SubjectGroup mGroup;

    private TextInputEditText mEditTextName;
    private TextInputLayout mTextInputName;
    private ViewGroup mContainer;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedState) {
        Activity activity = getActivity();
        assert activity != null;

        Bundle args = getArguments();
        mTimetablePath = args.getString(EXTRA_PATH);

        // Load extras
        if (savedState != null) {
            // SubjectTask must not be null here!
            //noinspection ConstantConditions
            mGroup = savedState.getParcelable(EXTRA_GROUP);
        } else {
            SubjectGroup group = args.getParcelable(EXTRA_GROUP);
            if (group != null) {
                mGroup = group.clone();
            } else mGroup = new SubjectGroup();
        }

        MaterialDialog md = new MaterialDialog.Builder(activity)
                .customView(R.layout.dialog_edit_subject_group, false)
                .title(TextUtils.isEmpty(mGroup.key)
                        ? R.string.dialog_group_new_title
                        : R.string.dialog_group_edit_title)
                .negativeText(android.R.string.cancel)
                .positiveText(TextUtils.isEmpty(mGroup.key)
                        ? R.string.dialog_add
                        : android.R.string.ok)
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(
                            @NonNull MaterialDialog dialog,
                            @NonNull DialogAction which) {
                        switch (which) {
                            case POSITIVE:
                                if (!validateName()) {
                                    return; // don't dismiss dialog
                                }

                                updateCurrentGroup();

                                // Filter empty users
                                for (int i = mGroup.users.size() - 1; i >= 0; i--) {
                                    String uid = mGroup.users.get(i);
                                    if (TextUtils.isEmpty(uid)
                                            || uid.length() < 5
                                            || uid.matches(Db.Restriction.REGEX_PATH_DISALLOWED)) {
                                        mGroup.users.remove(i);
                                    }
                                }

                                DatabaseReference subjectsRef = FirebaseDatabase.getInstance()
                                        .getReference(mTimetablePath)
                                        .child(Db.GROUPS);
                                if (TextUtils.isEmpty(mGroup.key)) {
                                    // Create new group
                                    DatabaseReference ref = subjectsRef.push();
                                    ref.setValue(mGroup);
                                    mGroup.key = ref.getKey();
                                } else subjectsRef.child(mGroup.key).setValue(mGroup);
                                break;
                        }
                        dismiss();
                    }
                })
                .autoDismiss(false)
                .build();

        View view = md.getCustomView();
        assert view != null;

        mContainer = (ViewGroup) view.findViewById(R.id.container);
        mEditTextName = (TextInputEditText) view.findViewById(R.id.input_name);
        mTextInputName = (TextInputLayout) view.findViewById(R.id.input_layout_name);
        mEditTextName.setText(mGroup.name);

        mEditTextName.addTextChangedListener(new Watcher(mEditTextName));

        view.findViewById(R.id.add).setOnClickListener(this);

        // Add existing users
        LayoutInflater inflater = LayoutInflater.from(getContext());
        if (mGroup.users != null) for (String uid : mGroup.users) {
            View v = inflater.inflate(R.layout.item_group_user_editable, mContainer, false);
            GroupUserEditableView user = (GroupUserEditableView) v;
            user.setOnActionButtonClickListener(this);
            user.setUser(uid);
            user.onwColumnsWithDetails();
            mContainer.addView(user);
        }

        return md;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.add) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View v = inflater.inflate(R.layout.item_group_user_editable, mContainer, false);

            GroupUserEditableView user = (GroupUserEditableView) v;
            user.setOnActionButtonClickListener(this);
            mContainer.addView(user);
        }
    }

    @Override
    public void onClick(
            @NonNull View view, @Nullable String uid,
            @NonNull GroupUserEditableView.ActionButton button) {
        switch (button) {
            case DELETE:
                // This view is a button, so go up in the hierarchy
                // until we find group-user-view.
                while (!(view instanceof GroupUserEditableView)) {
                    view = (View) view.getParent();
                }
                // We don't store uids in #mGroup yet, so
                // simple removing the view does the trick.
                mContainer.removeView(view);
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        updateCurrentGroup();
        outState.putParcelable(EXTRA_GROUP, mGroup);
    }

    private void updateCurrentGroup() {
        mGroup.name = getName();

        if (mGroup.users != null) {
            mGroup.users.clear();
        } else mGroup.users = new ArrayList<>();
        for (int i = 0; i < mContainer.getChildCount(); i++) {
            GroupUserEditableView userView = (GroupUserEditableView) mContainer.getChildAt(i);
            mGroup.users.add(userView.getUser());
        }
    }

    private boolean validateName() {
        if (TextUtils.isEmpty(getName())) {
            String errorMsg = getString(R.string.dialog_subject_error_enter_name);
            mEditTextName.requestFocus();
            mTextInputName.setError(errorMsg);
            return false;
        }

        mTextInputName.setErrorEnabled(false);
        return true;
    }

    /**
     * @return the name of the group
     * @see Subject#name
     */
    @NonNull
    private String getName() {
        return mEditTextName.getText().toString().trim();
    }

    /**
     * @author Artem Chepurnoy
     */
    private class Watcher implements TextWatcher {

        private final int viewId;

        private Watcher(View view) {
            this.viewId = view.getId();
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            // unused
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            // unused
        }

        public void afterTextChanged(Editable editable) {
            switch (viewId) {
                case R.id.input_name:
                    validateName();
                    break;
            }
        }
    }

}
