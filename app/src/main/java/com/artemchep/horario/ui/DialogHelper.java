package com.artemchep.horario.ui;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.artemchep.basic.ui.activities.ActivityBase;
import com.artemchep.horario.R;
import com.artemchep.horario.ui.fragments.dialogs.PrivacyPolicyDialog;
import com.artemchep.horario.ui.fragments.dialogs.base.DialogFragment;
import com.artemchep.horario.ui.widgets.SignInLayout;

/**
 * @author Artem Chepurnoy
 */
public class DialogHelper {

    private static final String TAG_FRAGMENT_PRIVACY = "dialog_privacy_policy";

    public static void showPrivacyDialog(@NonNull AppCompatActivity activity) {
        showDialog(activity, new PrivacyPolicyDialog(), TAG_FRAGMENT_PRIVACY);
    }
    
    private static void showDialog(@NonNull AppCompatActivity activity,
                                   @NonNull Class clazz,
                                   @NonNull String tag) {
        DialogFragment df;
        try {
            df = (DialogFragment) clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        showDialog(activity, df, tag);
    }

    private static void showDialog(@NonNull AppCompatActivity activity,
                                   @NonNull DialogFragment fragment,
                                   @NonNull String tag) {
        FragmentManager fm = activity.getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag(tag);
        if (prev != null) ft.remove(prev);
        ft.addToBackStack(null);
        fragment.show(ft, tag);
    }

}
