package com.artemchep.horario.ui.fragments.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.ContextThemeWrapper;

import com.afollestad.materialdialogs.MaterialDialog;
import com.artemchep.basic.utils.HtmlUtils;
import com.artemchep.basic.utils.PackageUtils;
import com.artemchep.basic.utils.RawReader;
import com.artemchep.horario.Config;
import com.artemchep.horario.R;
import com.artemchep.horario.ui.fragments.dialogs.base.DialogFragment;

/**
 * @author Artem Chepurnoy
 */
public class ChangelogDialog extends DialogFragment {

    /**
     * @return {@code true} if user has read current changelog,
     * {@code false} otherwise.
     */
    public static boolean isRead(@NonNull Context context) {
        PackageInfo pi;
        try {
            pi = context
                    .getPackageManager()
                    .getPackageInfo(PackageUtils.getName(context), 0);
        } catch (PackageManager.NameNotFoundException ignored) {
            return false;
        }

        return Config.getInstance().getInt(Config.KEY_CHANGELOG_READ) >= pi.versionCode;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ContextThemeWrapper context = getActivity();
        assert context != null;

        String src = RawReader.readText(getContext(), R.raw.changelog);
        String hint = RawReader.readText(getContext(), R.raw.changelog_hint);
        CharSequence message = HtmlUtils.fromLegacyHtml(hint + src);

        return new MaterialDialog.Builder(context)
                .title(getString(R.string.dialog_changelog_title))
                .content(message)
                .negativeText(R.string.dialog_close)
                .showListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        // Mark changelog as read

                        Context context = getContext();
                        PackageInfo pi;
                        try {
                            pi = context
                                    .getPackageManager()
                                    .getPackageInfo(PackageUtils.getName(context), 0);
                        } catch (PackageManager.NameNotFoundException ignored) {
                            return;
                        }

                        Config.getInstance()
                                .edit(context)
                                .put(Config.KEY_CHANGELOG_READ, pi.versionCode)
                                .commit();
                    }
                })
                .build();
    }

}