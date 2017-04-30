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
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.artemchep.horario.R;
import com.artemchep.horario.database.Db;
import com.artemchep.horario.models.Notification;
import com.artemchep.horario.ui.fragments.dialogs.base.DialogFragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Dialog for adding/editing existing
 * {@link Notification notification}.
 * You should put {@link #EXTRA_TIMETABLE_PATH}.
 *
 * @author Artem Chepurnoy
 */
public class NotificationDialog extends DialogFragment {

    public static final String EXTRA_TIMETABLE_PATH = "extra::timetable_path";
    public static final String EXTRA_NOTIFICATION = "extra::notification";

    private String mTimetablePath;
    private Notification mNotification;

    private TextInputEditText mEditTextTitle;
    private TextInputEditText mEditTextSummary;
    private TextInputLayout mTextInputTitle;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedState) {
        Activity activity = getActivity();
        assert activity != null;

        Bundle args = getArguments();
        mTimetablePath = args.getString(EXTRA_TIMETABLE_PATH);

        // Load extras
        if (savedState != null) {
            // Notification must not be null here!
            //noinspection ConstantConditions
            mNotification = savedState.getParcelable(EXTRA_NOTIFICATION);
        } else {
            Notification notification = args.getParcelable(EXTRA_NOTIFICATION);
            if (notification != null) {
                mNotification = notification;
            } else mNotification = new Notification();
        }

        // Load icon
        TypedArray a = getActivity().getTheme().obtainStyledAttributes(
                new int[]{R.attr.icon_bell});
        int iconDrawableRes = a.getResourceId(0, 0);
        a.recycle();

        MaterialDialog md = new MaterialDialog.Builder(activity)
                .customView(R.layout.dialog_edit_notification, true)
                .iconRes(iconDrawableRes)
                .title(TextUtils.isEmpty(mNotification.key)
                        ? R.string.dialog_subject_new_title
                        : R.string.dialog_subject_edit_title)
                .negativeText(android.R.string.cancel)
                .positiveText(android.R.string.ok)
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

                                updateCurrentNotification();

                                DatabaseReference subjectsRef = FirebaseDatabase.getInstance()
                                        .getReference(mTimetablePath)
                                        .child(Db.NOTIFICATIONS);
                                if (TextUtils.isEmpty(mNotification.key)) {
                                    // Create new notification
                                    DatabaseReference ref = subjectsRef.push();
                                    ref.setValue(mNotification);
                                    mNotification.key = ref.getKey();
                                } else subjectsRef.child(mNotification.key).setValue(mNotification);
                                break;
                        }
                        dismiss();
                    }
                })
                .autoDismiss(false)
                .build();

        View view = md.getCustomView();
        assert view != null;

        mEditTextTitle = (TextInputEditText) view.findViewById(R.id.input_title);
        mEditTextSummary = (TextInputEditText) view.findViewById(R.id.input_summary);

        mEditTextTitle.addTextChangedListener(new Watcher(mEditTextTitle));

        return md;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        updateCurrentNotification();
        outState.putParcelable(EXTRA_NOTIFICATION, mNotification);
    }

    private void updateCurrentNotification() {
        mNotification.title = getTitle();
        mNotification.summary = getSummary();
    }

    private boolean validateName() {
        /*
        if (TextUtils.isEmpty(getName())) {
            String errorMsg = getString(R.string.dialog_subject_error_enter_name);
            mEditTextName.requestFocus();
            mTextInputName.setError(errorMsg);
            return false;
        }

        mTextInputName.setErrorEnabled(false);
        */
        return true;
    }

    /**
     * @return the title of the notification
     * @see Notification#title
     */
    @NonNull
    private String getTitle() {
        return mEditTextTitle.getText().toString().trim();
    }

    /**
     * @return the summary of the notification
     * @see Notification#summary
     */
    @NonNull
    private String getSummary() {
        return mEditTextSummary.getText().toString().trim();
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
