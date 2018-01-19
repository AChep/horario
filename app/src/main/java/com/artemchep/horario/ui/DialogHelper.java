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
package com.artemchep.horario.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.artemchep.horario.ui.dialogs.AboutDialog;
import com.artemchep.horario.ui.dialogs.FeedbackDialog;
import com.artemchep.horario.ui.dialogs.HolidayModeDialog;
import com.artemchep.horario.ui.dialogs.PrivacyPolicyDialog;
import com.artemchep.horario.ui.dialogs.SubjectDialog;

/**
 * @author Artem Chepurnoy
 */
public class DialogHelper {

    private static final String TAG_FRAGMENT_PRIVACY = "dialog_privacy_policy";
    private static final String TAG_FRAGMENT_ABOUT = "dialog_about";
    private static final String TAG_FRAGMENT_FEEDBACK = "dialog_feedback";
    private static final String TAG_FRAGMENT_HOLIDAY_MODE = "dialog_holiday_mode";
    private static final String TAG_FRAGMENT_SUBJECT = "dialog_subject";

    public static void showAboutDialog(@NonNull AppCompatActivity activity) {
        showDialog(activity, new AboutDialog(), TAG_FRAGMENT_ABOUT);
    }

    public static void showPrivacyDialog(@NonNull AppCompatActivity activity) {
        showDialog(activity, new PrivacyPolicyDialog(), TAG_FRAGMENT_PRIVACY);
    }

    public static void showFeedbackDialog(@NonNull AppCompatActivity activity) {
        showDialog(activity, new FeedbackDialog(), TAG_FRAGMENT_FEEDBACK);
    }

    public static void showHolidayModeDialog(@NonNull AppCompatActivity activity) {
        showDialog(activity, new HolidayModeDialog(), TAG_FRAGMENT_HOLIDAY_MODE);
    }

    public static void showSubjectLocalDialog(@NonNull AppCompatActivity activity, @NonNull Bundle args) {
        DialogFragment fragment = new SubjectDialog();
        fragment.setArguments(args);
        showDialog(activity, fragment, TAG_FRAGMENT_SUBJECT);
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
