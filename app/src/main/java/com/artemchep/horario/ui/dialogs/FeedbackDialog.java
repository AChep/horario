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
package com.artemchep.horario.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Spinner;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.artemchep.basic.Device;
import com.artemchep.basic.utils.PackageUtils;
import com.artemchep.horario.Binfo;
import com.artemchep.horario.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import es.dmoral.toasty.Toasty;

/**
 * @author Artem Chepurnoy
 */
public class FeedbackDialog extends DialogFragment {

    /**
     * @return {@code true} if this device can send feedback email,
     * {@code false} otherwise.
     */
    public static boolean isSupported(@NonNull Context context) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle it
        PackageManager pm = context.getPackageManager();
        return pm != null && intent.resolveActivity(pm) != null;
    }

    private static final int MIN_MESSAGE_LENGTH = 15;

    private static final String EXTRA_TYPE = "type";
    private static final String EXTRA_MESSAGE = "message";

    private static final int TYPE_ISSUE = 0;
    private static final int TYPE_SUGGESTION = 1;
    private static final int TYPE_OTHER = 2;

    private Spinner mSpinner;

    private TextInputEditText mEditTextName;
    private TextInputLayout mTextInputName;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedState) {
        Activity activity = getActivity();
        assert activity != null;

        // Load icon
        TypedArray a = getActivity().getTheme().obtainStyledAttributes(
                new int[]{R.attr.icon_email});
        int iconDrawableRes = a.getResourceId(0, 0);
        a.recycle();

        MaterialDialog md = new MaterialDialog.Builder(activity)
                .iconRes(iconDrawableRes)
                .title(R.string.dialog_feedback_title)
                .customView(R.layout.dialog_feedback, true)
                .negativeText(android.R.string.cancel)
                .positiveText(R.string.dialog_send)
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(
                            @NonNull MaterialDialog dialog,
                            @NonNull DialogAction which) {
                        switch (which) {
                            case POSITIVE: {
                                if (!validateMessage()) {
                                    mEditTextName.requestFocus();
                                    return; // do not dismiss
                                }

                                Context context = getActivity();
                                CharSequence message = getMessage();
                                int type = mSpinner.getSelectedItemPosition();

                                CharSequence title = createTitle(context, type);
                                CharSequence body = createBody(context, message);
                                boolean sent = send(title, body);
                                if (!sent) {
                                    return; // do not dismiss
                                }
                            }
                        }
                        dismiss();
                    }
                })
                .autoDismiss(false)
                .build();

        View view = md.getCustomView();
        assert view != null;
        mSpinner = view.findViewById(R.id.type);
        mEditTextName = view.findViewById(R.id.input_message);
        mTextInputName = view.findViewById(R.id.input_layout_message);

        if (savedState != null) {
            mSpinner.setSelection(savedState.getInt(EXTRA_TYPE));
            mEditTextName.setText(savedState.getString(EXTRA_MESSAGE));
        }

        mEditTextName.addTextChangedListener(new Watcher(mEditTextName));

        return md;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_TYPE, mSpinner.getSelectedItemPosition());
        outState.putString(EXTRA_MESSAGE, getMessage());
    }

    private boolean send(@NonNull CharSequence title, @NonNull CharSequence body) {
        Activity activity = getActivity();
        String[] recipients = {Binfo.SUPPORT_EMAIL};
        Intent intent = new Intent()
                .putExtra(Intent.EXTRA_EMAIL, recipients)
                .putExtra(Intent.EXTRA_SUBJECT, title)
                .putExtra(Intent.EXTRA_TEXT, body);
        intent.setAction(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle it

        try {
            startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            Toasty.error(activity, getString(R.string.feedback_error_no_app), Toast.LENGTH_LONG).show();
            return false;
        }
    }

    /**
     * Creates the name of the email.
     *
     * @param type one of the following types:
     *             0 - issue
     *             1 - suggestion
     *             2 - other
     * @return the name of the email.
     */
    @NonNull
    private CharSequence createTitle(@NonNull Context context, int type) {
        CharSequence osVersion = Device.API_VERSION_NAME_SHORT;
        CharSequence msgTypeStr;
        switch (type) {
            case TYPE_ISSUE:
                msgTypeStr = "issue";
                break;
            case TYPE_SUGGESTION:
                msgTypeStr = "suggestion";
                break;
            case TYPE_OTHER:
            default:
                msgTypeStr = "other";
                break;
        }

        // Get version name
        String versionName;
        try {
            PackageManager pm = context.getPackageManager();
            String packageName = context.getPackageName();
            PackageInfo info = pm.getPackageInfo(packageName, 0);
            versionName = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = "N/A";
        }

        return String.format("%s v%s: %s, %s",
                getString(R.string.app_name),
                versionName, osVersion, msgTypeStr);
    }

    /**
     * Creates the body of the email. It automatically adds some
     * info about the device.
     *
     * @param msg the message that been typed by user.
     * @return the body of the email
     */
    @NonNull
    private CharSequence createBody(@NonNull Context context, @NonNull CharSequence msg) {
        final String extra;

        do {
            PackageInfo pi;
            try {
                pi = context
                        .getPackageManager()
                        .getPackageInfo(PackageUtils.getName(context), 0);
            } catch (PackageManager.NameNotFoundException e) {
                extra = "There was an exception while getting my own package info.";
                break;
            }

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            JSONObject obj = new JSONObject();
            try {
                // App related stuff
                obj.put("app_version_code", pi.versionCode);
                obj.put("app_version_name", pi.versionName);
                obj.put("app_is_debug", Binfo.DEBUG);
                obj.put("app_user_id", user != null ? user.getUid() : "signed-out");

                // Device related stuff
                obj.put("language", Locale.getDefault().getLanguage());
                obj.put("android_version_release", android.os.Build.VERSION.RELEASE);
                obj.put("android_version_sdk_int", android.os.Build.VERSION.SDK_INT);
                obj.put("android_build_display", android.os.Build.DISPLAY);
                obj.put("android_build_brand", android.os.Build.BRAND);
                obj.put("android_build_model", android.os.Build.MODEL);
            } catch (JSONException ignored) {
                extra = "There was an exception while building JSON.";
                break;
            }

            extra = obj.toString().replaceAll(",\"", ", \"");
        } while (false);

        return msg + "\n\nDevice configuration (added automatically & do not change):\n" + extra;
    }

    private boolean validateMessage() {
        if (getMessage().length() < MIN_MESSAGE_LENGTH) {
            mTextInputName.setError(getString(
                    R.string.feedback_error_msg_too_short,
                    MIN_MESSAGE_LENGTH));
            return false;
        }

        mTextInputName.setErrorEnabled(false);
        return true;
    }

    /**
     * @return the message
     */
    @NonNull
    private String getMessage() {
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
                case R.id.input_message:
                    validateMessage();
                    break;
            }
        }
    }

}
