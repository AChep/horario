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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Spanned;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.artemchep.basic.utils.HtmlUtils;
import com.artemchep.basic.utils.RawReader;
import com.artemchep.horario.Binfo;
import com.artemchep.horario.R;
import com.artemchep.horario.ui.fragments.dialogs.base.DialogFragment;
import com.artemchep.horario.utils.ToastUtils;

import es.dmoral.toasty.Toasty;

/**
 * @author Artem Chepurnoy
 */
public class AboutDialog extends DialogFragment {

    private static final String PLAY_STORE_URL = "https://play.google.com/store/apps/details" +
            "?id=com.artemchep.horario";

    private Toast mTimeStampToast;

    /**
     * @author Artem Chepurnoy
     */
    private enum DialogView {
        ABOUT,
        CHANGELOG,
    }

    @NonNull
    public static Spanned getFormattedVersionName(@NonNull Context context) {
        PackageManager pm = context.getPackageManager();
        String packageName = context.getPackageName();
        String versionName;
        try {
            PackageInfo info = pm.getPackageInfo(packageName, 0);
            versionName = info.versionName;

            // Make the info part of version name a bit smaller.
            if (versionName.indexOf('-') >= 0) {
                versionName = versionName.replaceFirst("\\-", "<small>-") + "</small>";
            }
        } catch (PackageManager.NameNotFoundException e) {
            versionName = "N/A";
        }

        // TODO: Get the tint color from current theme.
        int color = 0xFF888888;

        Resources res = context.getResources();
        String html = res.getString(R.string.dialog_about_title,
                res.getString(R.string.app_name),
                versionName,
                Integer.toHexString(Color.red(color))
                        + Integer.toHexString(Color.green(color))
                        + Integer.toHexString(Color.blue(color)));
        return HtmlUtils.fromLegacyHtml(html);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ContextThemeWrapper context = getActivity();
        assert context != null;

        String year = Binfo.TIME_STAMP_YEAR;
        String credits = getString(R.string.dialog_about_credits);
        String src = getString(R.string.dialog_about_message, credits, year);
        CharSequence message = HtmlUtils.fromLegacyHtml(src);

        // Load icon
        TypedArray a = getActivity().getTheme().obtainStyledAttributes(
                new int[]{R.attr.icon_information_outline});
        int iconDrawableRes = a.getResourceId(0, 0);
        a.recycle();

        MaterialDialog md = new MaterialDialog.Builder(context)
                .iconRes(iconDrawableRes)
                .title(getFormattedVersionName(context))
                .content(message)
                .negativeText(R.string.dialog_close)
                .positiveText(R.string.dialog_share)
                .neutralText(R.string.dialog_changelog_title)
                .onAny(new MaterialDialog.SingleButtonCallback() {

                    private DialogView mCurView = DialogView.ABOUT;

                    @Override
                    public void onClick(
                            @NonNull MaterialDialog dialog,
                            @NonNull DialogAction which) {
                        switch (which) {
                            case NEUTRAL: {
                                if (mCurView == DialogView.ABOUT) {
                                    mCurView = DialogView.CHANGELOG;
                                    switchToChangelogView(dialog);
                                } else {
                                    mCurView = DialogView.ABOUT;
                                    switchToAboutView(dialog);
                                }
                            }
                            return;
                            case POSITIVE: {
                                try {
                                    Intent i = new Intent(Intent.ACTION_SEND);
                                    i.setType("text/plain");
                                    i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                                    i.putExtra(Intent.EXTRA_TEXT, "Horario is an open source app for " +
                                            "managing your school or university life: " + PLAY_STORE_URL);
                                    startActivity(Intent.createChooser(i, getString(R.string.dialog_share_horario)));
                                } catch (Exception e) {
                                }
                                break;
                            }
                        }
                        dismiss();
                    }
                })
                .autoDismiss(false)
                .build();
        md.getTitleView().setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ToastUtils.cancel(mTimeStampToast);
                mTimeStampToast = Toasty.info(getContext(), Binfo.TIME_STAMP);
                mTimeStampToast.show();
            }

        });
        return md;
    }

    private void switchToAboutView(@NonNull MaterialDialog dialog) {
        String year = Binfo.TIME_STAMP_YEAR;
        String credits = getString(R.string.dialog_about_credits);
        String src = getString(R.string.dialog_about_message, credits, year);
        CharSequence message = HtmlUtils.fromLegacyHtml(src);
        dialog.getTitleView().setText(getFormattedVersionName(getContext()));
        dialog.getContentView().setText(message);
        dialog.getActionButton(DialogAction.NEUTRAL).setText(R.string.nav_changelog);
    }

    private void switchToChangelogView(@NonNull MaterialDialog dialog) {
        String src = RawReader.readText(getContext(), R.raw.changelog);
        CharSequence message = HtmlUtils.fromLegacyHtml(src);
        dialog.getTitleView().setText(R.string.dialog_changelog_title);
        dialog.getContentView().setText(message);
        dialog.getActionButton(DialogAction.NEUTRAL).setText(R.string.nav_about);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        ToastUtils.cancel(mTimeStampToast);
    }

}
