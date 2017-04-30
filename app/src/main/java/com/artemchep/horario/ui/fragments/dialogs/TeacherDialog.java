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
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.HorizontalScrollView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.artemchep.horario.Palette;
import com.artemchep.horario.R;
import com.artemchep.horario.database.Db;
import com.artemchep.horario.models.Teacher;
import com.artemchep.horario.ui.fragments.dialogs.base.DialogFragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.thebluealliance.spectrum.SpectrumPalette;

import static com.artemchep.horario.Palette.PALETTE;

/**
 * Dialog for adding/editing existing
 * {@link com.artemchep.horario.models.Teacher teacher}.
 * You should put {@link #EXTRA_TIMETABLE_PATH}.
 *
 * @author Artem Chepurnoy
 */
public class TeacherDialog extends DialogFragment implements SpectrumPalette.OnColorSelectedListener {

    public static final String EXTRA_TIMETABLE_PATH = "extra::timetable_path";
    public static final String EXTRA_TEACHER = "teacher";

    private String mTimetablePath;
    private Teacher mTeacher;

    private TextInputEditText mEditTextName;
    private TextInputEditText mEditTextEmail;
    private TextInputEditText mEditTextPhone;
    private TextInputEditText mEditTextInfo;
    private TextInputLayout mTextInputName;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedState) {
        Activity activity = getActivity();
        assert activity != null;

        Bundle args = getArguments();
        mTimetablePath = args.getString(EXTRA_TIMETABLE_PATH);

        // Load extras
        if (savedState != null) {
            // Subject must not be null here!
            //noinspection ConstantConditions
            mTeacher = savedState.getParcelable(EXTRA_TEACHER);
        } else {
            Teacher teacher = args.getParcelable(EXTRA_TEACHER);
            if (teacher != null) {
                mTeacher = teacher;
            } else mTeacher = new Teacher();
        }

        boolean isCreatingNewTeacher = TextUtils.isEmpty(mTeacher.key);

        // Load icon
        TypedArray a = getActivity().getTheme().obtainStyledAttributes(
                new int[]{isCreatingNewTeacher
                        ? R.attr.icon_account_plus
                        : R.attr.icon_account_edit});
        int iconDrawableRes = a.getResourceId(0, 0);
        a.recycle();

        MaterialDialog md = new MaterialDialog.Builder(activity)
                .customView(R.layout.dialog_edit_teacher, true)
                .iconRes(iconDrawableRes)
                .title(isCreatingNewTeacher
                        ? R.string.dialog_teacher_new_title
                        : R.string.dialog_teacher_edit_title)
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
                                    return; // do not dismiss dialog
                                }

                                updateCurrentTeacher();

                                DatabaseReference teachersRef = FirebaseDatabase.getInstance()
                                        .getReference(mTimetablePath)
                                        .child(Db.TEACHERS);
                                if (TextUtils.isEmpty(mTeacher.key)) {
                                    // Create new teacher
                                    DatabaseReference ref = teachersRef.push();
                                    ref.setValue(mTeacher);
                                    mTeacher.key = ref.getKey();
                                } else teachersRef.child(mTeacher.key).setValue(mTeacher);
                            case NEGATIVE:
                                dismiss();
                                break;
                        }
                    }
                })
                .autoDismiss(false)
                .build();

        View view = md.getCustomView();
        assert view != null;
        mEditTextName = (TextInputEditText) view.findViewById(R.id.input_name);
        mEditTextEmail = (TextInputEditText) view.findViewById(R.id.input_email);
        mEditTextPhone = (TextInputEditText) view.findViewById(R.id.input_phone);
        mEditTextInfo = (TextInputEditText) view.findViewById(R.id.input_info);
        mTextInputName = (TextInputLayout) view.findViewById(R.id.input_layout_name);
        mEditTextName.setText(mTeacher.name);
        mEditTextEmail.setText(mTeacher.email);
        mEditTextPhone.setText(mTeacher.phone);
        mEditTextInfo.setText(mTeacher.info);

        mEditTextName.addTextChangedListener(new Watcher(mEditTextName));

        final SpectrumPalette paletteView = (SpectrumPalette) view.findViewById(R.id.palette);
        paletteView.setOnColorSelectedListener(this);
        paletteView.setFixedColumnCount(PALETTE.length);
        paletteView.setColors(PALETTE);
        paletteView.setSelectedColor(Palette.findColorByHue(PALETTE, mTeacher.color));

        // Automatically scroll palette view
        // to ensure that selected item is shown on start
        paletteView.post(new Runnable() {
            @Override
            public void run() {
                int[] p = PALETTE;
                int width = paletteView.getMeasuredWidth() / p.length;
                int pos = 0;

                for (int i = 0; i < p.length; i++) {
                    if (mTeacher.color == p[i]) {
                        pos = i;
                        break;
                    }
                }

                HorizontalScrollView scrollView = (HorizontalScrollView) paletteView.getParent();
                scrollView.scrollBy(width * pos, 0);
            }
        });

        return md;
    }

    @Override
    public void onColorSelected(@ColorInt int color) {
        mTeacher.color = color;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        updateCurrentTeacher();
        outState.putParcelable(EXTRA_TEACHER, mTeacher);
    }

    private void updateCurrentTeacher() {
        mTeacher.name = getName();
        mTeacher.email = getEmail();
        mTeacher.phone = getPhoneNumber();
        mTeacher.info = getInfo();
    }

    private boolean validateName() {
        if (mEditTextName.getText().toString().trim().isEmpty()) {
            String errorMsg = getString(R.string.dialog_teacher_error_enter_name);
            mEditTextName.requestFocus();
            mTextInputName.setError(errorMsg);
            return false;
        }

        mTextInputName.setErrorEnabled(false);
        return true;
    }

    /**
     * @return the name of the teacher
     * @see Teacher#name
     */
    @NonNull
    private String getName() {
        return mEditTextName.getText().toString().trim();
    }

    /**
     * @return the email of the teacher
     * @see Teacher#email
     */
    @NonNull
    private String getEmail() {
        return mEditTextEmail.getText().toString().trim();
    }

    /**
     * @return the phone number of the teacher
     * @see Teacher#phone
     */
    @NonNull
    private String getPhoneNumber() {
        return mEditTextPhone.getText().toString().trim();
    }

    /**
     * @return the info about the teacher
     * @see Teacher#info
     */
    @NonNull
    private String getInfo() {
        return mEditTextInfo.getText().toString().trim();
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
