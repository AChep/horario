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

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;

import com.afollestad.materialdialogs.MaterialDialog;
import com.artemchep.basic.async.AsyncTask;
import com.artemchep.basic.utils.HtmlUtils;
import com.artemchep.basic.utils.IOUtils;
import com.artemchep.basic.utils.NetworkUtils;
import com.artemchep.basic.utils.PackageUtils;
import com.artemchep.basic.utils.RawReader;
import com.artemchep.horario.R;
import com.artemchep.horario.ui.fragments.dialogs.base.DialogFragment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.URL;

import timber.log.Timber;

/**
 * @author Artem Chepurnoy
 */
public class PrivacyPolicyDialog extends DialogFragment {

    private static final String FILENAME = "privacy_policy";
    private static final String KEY_VERSION = "version";
    private static final String KEY_PRIVACY_POLICY = "privacy_policy";

    private T mUpdateTask;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ContextThemeWrapper context = getActivity();
        assert context != null;

        // Load icon
        TypedArray a = getActivity().getTheme().obtainStyledAttributes(
                new int[]{R.attr.icon_shield});
        int iconDrawableRes = a.getResourceId(0, 0);
        a.recycle();

        final String source = getPrivacyPolicy();
        CharSequence privacyPolicy = HtmlUtils.fromLegacyHtml(source);

        return new MaterialDialog.Builder(context)
                .iconRes(iconDrawableRes)
                .title(R.string.dialog_privacy_policy)
                .content(privacyPolicy)
                .negativeText(R.string.dialog_close)
                .build();
    }

    @Nullable
    private String getPrivacyPolicy() {
        retrieve_cached_privacy_policy:
        {
            Context context = getContext();
            PackageInfo pi;
            try {
                pi = context
                        .getPackageManager()
                        .getPackageInfo(PackageUtils.getName(context), 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                break retrieve_cached_privacy_policy;
            }

            SharedPreferences sp = getContext().getSharedPreferences(FILENAME, 0);
            int version = sp.getInt(KEY_VERSION, -1);
            String text = sp.getString(KEY_PRIVACY_POLICY, null);

            if (version >= pi.versionCode && text != null) {
                return text;
            }
        }
        return RawReader.readText(getContext(), R.raw.privacy_policy);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (NetworkUtils.isOnline(getContext())) {
            mUpdateTask = new T(this);
            mUpdateTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        AsyncTask.stop(mUpdateTask);
    }

    /**
     * Stores new privacy policy in shared preferences and
     * updates user interface.
     */
    private void handleNewPrivacyPolicy(@NonNull String text) {
        Context context = getContext();
        PackageInfo pi;
        try {
            pi = context
                    .getPackageManager()
                    .getPackageInfo(PackageUtils.getName(context), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return;
        }

        SharedPreferences sp = getContext().getSharedPreferences(FILENAME, 0);
        sp.edit()
                .putInt(KEY_VERSION, pi.versionCode)
                .putString(KEY_PRIVACY_POLICY, text)
                .apply();

        MaterialDialog md = (MaterialDialog) getDialog();
        md.getContentView().setText(HtmlUtils.fromLegacyHtml(text));
    }

    /**
     * @author Artem Chepurnoy
     */
    private static class T extends AsyncTask<Void, Void, String> {

        private static final String TAG = "PrivacyPolicyUpdater";
        private static final String PRIVACY_POLICY_URL = "https://raw.githubusercontent.com/XJSHQ/horario/master/app/src/main/res/raw/privacy_policy.xml";

        @NonNull
        private final WeakReference<PrivacyPolicyDialog> mDialogRef;

        T(@NonNull PrivacyPolicyDialog dialog) {
            mDialogRef = new WeakReference<>(dialog);
        }

        @Override
        protected String doInBackground(Void... params) {
            return download(PRIVACY_POLICY_URL);
        }

        @Nullable
        private String download(@NonNull String url) {
            InputStream is = null;
            InputStreamReader isr = null;
            BufferedReader br = null;
            try {
                is = new URL(url).openStream();
                isr = new InputStreamReader(is);
                br = new BufferedReader(isr);
                return IOUtils.readTextFromBufferedReader(br);
            } catch (IOException e) {
                Timber.tag(TAG).w("Failed fetching from " + url);
            } finally {
                try {
                    if (br != null) {
                        br.close();
                    } else if (isr != null) {
                        isr.close();
                    } else if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            PrivacyPolicyDialog dialog = mDialogRef.get();
            if (dialog != null && !TextUtils.isEmpty(s)) {
                dialog.handleNewPrivacyPolicy(s);
            }
        }

    }

}
