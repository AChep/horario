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
import com.artemchep.horario.models.Subject;
import com.artemchep.horario.ui.fragments.dialogs.base.DialogFragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.thebluealliance.spectrum.SpectrumPalette;

import static com.artemchep.horario.Palette.PALETTE;

/**
 * Dialog for adding/editing existing
 * {@link com.artemchep.horario.models.Subject subject}.
 * You should put {@link #EXTRA_TIMETABLE_PATH}.
 *
 * @author Artem Chepurnoy
 */
public class SubjectDialog extends DialogFragment implements SpectrumPalette.OnColorSelectedListener {

    public static final String EXTRA_TIMETABLE_PATH = "extra::timetable_path";
    public static final String EXTRA_SUBJECT = "subject";

    private String mTimetablePath;
    private Subject mSubject;

    private TextInputEditText mEditTextName;
    private TextInputEditText mEditTextAbbr;
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
            mSubject = savedState.getParcelable(EXTRA_SUBJECT);
        } else {
            Subject subject = args.getParcelable(EXTRA_SUBJECT);
            if (subject != null) {
                mSubject = subject;
            } else mSubject = new Subject();
        }

        // Load icon
        TypedArray a = getActivity().getTheme().obtainStyledAttributes(
                new int[]{R.attr.icon_label});
        int iconDrawableRes = a.getResourceId(0, 0);
        a.recycle();

        MaterialDialog md = new MaterialDialog.Builder(activity)
                .customView(R.layout.dialog_edit_subject, true)
                .iconRes(iconDrawableRes)
                .title(TextUtils.isEmpty(mSubject.key)
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

                                updateCurrentSubject();

                                DatabaseReference subjectsRef = FirebaseDatabase.getInstance()
                                        .getReference(mTimetablePath)
                                        .child(Db.SUBJECTS);
                                if (TextUtils.isEmpty(mSubject.key)) {
                                    // Create new subject
                                    DatabaseReference ref = subjectsRef.push();
                                    ref.setValue(mSubject);
                                    mSubject.key = ref.getKey();
                                } else subjectsRef.child(mSubject.key).setValue(mSubject);
                                break;
                        }
                        dismiss();
                    }
                })
                .autoDismiss(false)
                .build();

        View view = md.getCustomView();
        assert view != null;

        mEditTextName = (TextInputEditText) view.findViewById(R.id.input_name);
        mEditTextAbbr = (TextInputEditText) view.findViewById(R.id.input_abbr);
        mEditTextInfo = (TextInputEditText) view.findViewById(R.id.input_info);
        mTextInputName = (TextInputLayout) view.findViewById(R.id.input_layout_name);
        mEditTextName.setText(mSubject.name);
        mEditTextAbbr.setText(mSubject.abbreviation);
        mEditTextInfo.setText(mSubject.info);

        mEditTextName.addTextChangedListener(new Watcher(mEditTextName));

        final SpectrumPalette paletteView = (SpectrumPalette) view.findViewById(R.id.palette);
        paletteView.setOnColorSelectedListener(this);
        paletteView.setFixedColumnCount(PALETTE.length);
        paletteView.setColors(PALETTE);
        paletteView.setSelectedColor(Palette.findColorByHue(PALETTE, mSubject.color));

        // Automatically scroll palette view
        // to ensure that selected item is shown on start
        paletteView.post(new Runnable() {
            @Override
            public void run() {
                int[] p = PALETTE;
                int width = paletteView.getMeasuredWidth() / p.length;
                int pos = 0;

                for (int i = 0; i < p.length; i++) {
                    if (mSubject.color == p[i]) {
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
        mSubject.color = color;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        updateCurrentSubject();
        outState.putParcelable(EXTRA_SUBJECT, mSubject);
    }

    private void updateCurrentSubject() {
        mSubject.name = getName();
        mSubject.abbreviation = getAbbreviation();
        mSubject.info = getInfo();
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
     * @return the name of the subject
     * @see Subject#name
     */
    @NonNull
    private String getName() {
        return mEditTextName.getText().toString().trim();
    }

    /**
     * @return the abbreviation of the subject
     * @see Subject#abbreviation
     */
    @NonNull
    private String getAbbreviation() {
        return mEditTextAbbr.getText().toString().trim();
    }

    /**
     * @return the information about the subject
     * @see Subject#info
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
