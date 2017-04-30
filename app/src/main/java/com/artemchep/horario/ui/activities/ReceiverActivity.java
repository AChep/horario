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
package com.artemchep.horario.ui.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.artemchep.basic.ui.activities.ActivityBase;
import com.artemchep.horario.Config;
import com.artemchep.horario.R;
import com.artemchep.horario.ShareHelper;
import com.artemchep.horario.analytics.AnalyticsEvent;
import com.artemchep.horario.analytics.AnalyticsParam;
import com.artemchep.horario.database.Address;
import com.artemchep.horario.database.Db;
import com.artemchep.horario.models.Timetable;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;

/**
 * @author Artem Chepurnoy
 */
public class ReceiverActivity extends ActivityBase implements
        DialogInterface.OnDismissListener,
        MaterialDialog.SingleButtonCallback {

    private static final String TAG = "ReceiverActivity";

    private static final String EXTRA_TIMETABLE = "extra::timetable";

    /**
     * Creates a clickable shareable link
     * that contains shared timetable.
     */
    @NonNull
    public static String createShareLink(@NonNull String key) {
        return "http://artemchep.com/"
                + PATH_SEG_1_HORARIO + "/"
                + PATH_SEG_2_SHARE + "/"
                + key;
    }

    private static final String PATH_SEG_1_HORARIO = "horario";
    private static final String PATH_SEG_2_SHARE = "share";

    private String mUserId;
    private Timetable mTimetable;
    private FirebaseAnalytics mAnalytics;

    private TextInputLayout mTextInputName;
    private TextInputEditText mEditTextName;

    @NonNull
    private final ShareHelper mShareHelper = new ShareHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAnalytics = FirebaseAnalytics.getInstance(this);

        if (savedInstanceState != null) {
            // Timetable must not be null here!
            //noinspection ConstantConditions
            mTimetable = savedInstanceState.getParcelable(EXTRA_TIMETABLE);
        } else mTimetable = new Timetable();

        // Load icon
        TypedArray a = getActivity().getTheme().obtainStyledAttributes(
                new int[]{R.attr.icon_timetable});
        int iconDrawableRes = a.getResourceId(0, 0);
        a.recycle();

        MaterialDialog md = new MaterialDialog.Builder(this)
                .iconRes(iconDrawableRes)
                .title(R.string.dialog_timetable_new_title)
                .customView(R.layout.dialog_timetable, true)
                .negativeText(android.R.string.cancel)
                .positiveText(R.string.dialog_add)
                .dismissListener(this)
                .onAny(this)
                .autoDismiss(false)
                .build();

        View view = md.getCustomView();
        assert view != null;
        mEditTextName = (TextInputEditText) view.findViewById(R.id.input_name);
        mTextInputName = (TextInputLayout) view.findViewById(R.id.input_layout_name);
        mEditTextName.setText(mTimetable.name);
        mEditTextName.addTextChangedListener(new Watcher(mEditTextName));

        Bundle analytics = new Bundle();
        Intent intent = getIntent();
        Uri data;

        if (intent != null
                && (data = intent.getData()) != null
                && Intent.ACTION_VIEW.equals(intent.getAction())) {
            final List<String> segments = data.getPathSegments();
            if (segments.size() == 3
                    && segments.get(0).equals(PATH_SEG_1_HORARIO)
                    && segments.get(1).equals(PATH_SEG_2_SHARE)) {
                String key = segments.get(2);
                mShareHelper.decode(key);

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null && mShareHelper.isCorrect()) {
                    mTimetable.publicAddress = mShareHelper.getPublicAddress();
                    mTimetable.isCopy = true;
                    mUserId = user.getUid();

                    // Show timetable dialog and
                    // do not finish activity
                    md.show();

                    analytics.putBoolean(AnalyticsParam.CORRECT, true);
                    mAnalytics.logEvent(AnalyticsEvent.IMPORT_TIMETABLE, analytics);
                    return;
                }
            }
        }

        analytics.putBoolean(AnalyticsParam.CORRECT, false);
        mAnalytics.logEvent(AnalyticsEvent.IMPORT_TIMETABLE, analytics);

        Toasty.warning(this, getString(R.string.dialog_timetable_error_corrupted_link)).show();
        finish();
    }

    @Override
    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
        switch (which) {
            case POSITIVE:
                if (!validateName()) {
                    return; // do not dismiss dialog
                }

                save();
            case NEUTRAL:
                break;
        }

        dialog.dismiss();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        supportFinishAfterTransition();
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

    /**
     * Note that you should always validate data before
     * calling this method.
     */
    private void save() {
        updateCurrentTimetable();

        DatabaseReference userRef = Db.user(mUserId).ref();
        // Generate keys
        mTimetable.key = userRef.child(Db.TIMETABLES).push().getKey();
        mTimetable.privateKey = userRef.child(Db.TIMETABLES_PRIVATE).push().getKey();
        // Commit
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/" + Db.TIMETABLES + "/" + mTimetable.key, mTimetable);
        childUpdates.put("/" + Db.TIMETABLES_PRIVATE + "/" + mTimetable.privateKey + "/tmp", 1);
        userRef.updateChildren(childUpdates);

        // Automatically select created timetable
        // if no one is selected.
        Config config = Config.getInstance();
        String address = config.getString(Config.KEY_ADDRESS);
        if (Address.EMPTY.equals(address)) {
            address = Address.toString(Address.fromModel(mTimetable));
            config
                    .edit(this)
                    .put(Config.KEY_ADDRESS, address)
                    .commit();
        }
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
