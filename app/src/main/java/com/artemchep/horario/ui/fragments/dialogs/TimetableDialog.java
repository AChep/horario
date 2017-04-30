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
import android.text.TextWatcher;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.artemchep.horario.Config;
import com.artemchep.horario.R;
import com.artemchep.horario.database.Address;
import com.artemchep.horario.database.Db;
import com.artemchep.horario.models.Timetable;
import com.artemchep.horario.ui.fragments.dialogs.base.DialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

/**
 * @author Artem Chepurnoy
 */
public class TimetableDialog extends DialogFragment {

    public static final String EXTRA_USER_ID = "user:id";
    public static final String EXTRA_TIMETABLE = "timetable";

    private static final String TAG = "TimetableDialog";

    /**
     * Current user's id. Note that user may differ from one returned by
     * {@link FirebaseAuth#getCurrentUser()}.
     */
    private String mUserId;
    private Timetable mTimetable = new Timetable();
    private boolean mDone;

    private TextInputEditText mEditTextName;
    private TextInputLayout mTextInputName;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedState) {
        Activity activity = getActivity();
        assert activity != null;

        Bundle args = getArguments();
        assert args != null;
        mUserId = args.getString(EXTRA_USER_ID);
        Timetable timetable = args.getParcelable(EXTRA_TIMETABLE);
        if (timetable != null) {
            mTimetable = timetable.clone();
        }

        if (savedState != null) {
            // Timetable must not be null here!
            //noinspection ConstantConditions
            mTimetable = savedState.getParcelable(EXTRA_TIMETABLE);
        }

        // Load icon
        TypedArray a = getActivity().getTheme().obtainStyledAttributes(
                new int[]{R.attr.icon_timetable});
        int iconDrawableRes = a.getResourceId(0, 0);
        a.recycle();

        MaterialDialog md = new MaterialDialog.Builder(activity)
                .customView(R.layout.dialog_timetable, true)
                .iconRes(iconDrawableRes)
                .title(mTimetable.key == null
                        ? R.string.dialog_timetable_new_title
                        : R.string.dialog_timetable_edit_title)
                .negativeText(android.R.string.cancel)
                .positiveText(R.string.dialog_save)
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(
                            @NonNull MaterialDialog dialog,
                            @NonNull DialogAction which) {
                        switch (which) {
                            case POSITIVE:
                                if (!validateName() || mDone) {
                                    return; // do not dismiss dialog
                                }

                                save();

                                mDone = true;
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
        mTextInputName = (TextInputLayout) view.findViewById(R.id.input_layout_name);
        mEditTextName.setText(mTimetable.name);
        mEditTextName.addTextChangedListener(new Watcher(mEditTextName));
        return md;
    }

    /**
     * Note that you should always validate data before
     * calling this method.
     */
    private void save() {
        updateCurrentTimetable();

        Timber.tag(TAG).d("Timetable SAVING"
                + " key=" + mTimetable.key
                + " name=" + mTimetable.name
                + " uid=" + mUserId);

        if (mTimetable.key != null) {
            // Update existing timetable instead of
            // creating a new one.
            Db.user(mUserId).timetable(mTimetable.key).ref().setValue(mTimetable);
            return;
        }

        DatabaseReference userRef = Db.user(mUserId).ref();
        // Generate keys
        String privateKey = userRef.child("timetable_private").push().getKey();
        String publicKey = userRef.child("timetable_public").push().getKey();
        mTimetable.key = userRef.child("timetable").push().getKey();
        mTimetable.privateKey = privateKey;
        mTimetable.publicAddress = mUserId + "/" + publicKey;
        // Commit
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/timetable/" + mTimetable.key, mTimetable);
        childUpdates.put("/timetable_private/" + privateKey + "/tmp", 1);
        childUpdates.put("/timetable_public/" + publicKey + "/tmp", 1);
        childUpdates.put("/timetable_public/" + publicKey + "/name", mTimetable.name);
        userRef.updateChildren(childUpdates);

        // Automatically select created timetable
        // if no one is selected.
        Config config = Config.getInstance();
        String address = config.getString(Config.KEY_ADDRESS);
        if (Address.EMPTY.equals(address)) {
            address = Address.toString(Address.fromModel(mTimetable));
            config
                    .edit(getContext())
                    .put(Config.KEY_ADDRESS, address)
                    .commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mDone = false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        updateCurrentTimetable();
        outState.putParcelable(EXTRA_TIMETABLE, mTimetable);
    }

    private void updateCurrentTimetable() {
        mTimetable.name = getName();
    }

    private boolean validateName() {
        if (getName().isEmpty()) {
            String errorMsg = getString(R.string.dialog_timetable_error_enter_name);
            mEditTextName.requestFocus();
            mTextInputName.setError(errorMsg);
            return false;
        }

        mTextInputName.setErrorEnabled(false);
        return true;
    }

    /**
     * @return the name of the timetable
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