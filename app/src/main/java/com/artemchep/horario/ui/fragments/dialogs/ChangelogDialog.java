package com.artemchep.horario.ui.fragments.dialogs;

import android.app.Dialog;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.ContextThemeWrapper;

import com.afollestad.materialdialogs.MaterialDialog;
import com.artemchep.basic.utils.HtmlUtils;
import com.artemchep.basic.utils.RawReader;
import com.artemchep.horario.R;
import com.artemchep.horario.ui.fragments.dialogs.base.DialogFragment;

/**
 * @author Artem Chepurnoy
 */
public class ChangelogDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ContextThemeWrapper context = getActivity();
        assert context != null;

        String src = RawReader.readText(getContext(), R.raw.changelog);
        CharSequence message = HtmlUtils.fromLegacyHtml(src);

        return new MaterialDialog.Builder(context)
                .title(getString(R.string.dialog_changelog_title))
                .content(message)
                .negativeText(R.string.dialog_close)
                .build();
    }

}