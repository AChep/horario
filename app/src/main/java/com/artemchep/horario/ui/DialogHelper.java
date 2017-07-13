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
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.artemchep.horario.database.models.SubjectGroup;
import com.artemchep.horario.models.Absence;
import com.artemchep.horario.models.Exam;
import com.artemchep.horario.models.Lesson;
import com.artemchep.horario.models.Notification;
import com.artemchep.horario.models.Subject;
import com.artemchep.horario.models.SubjectTask;
import com.artemchep.horario.models.Teacher;
import com.artemchep.horario.models.Timetable;
import com.artemchep.horario.ui.fragments.dialogs.AboutDialog;
import com.artemchep.horario.ui.fragments.dialogs.AbsenceDialog;
import com.artemchep.horario.ui.fragments.dialogs.ExamDialog;
import com.artemchep.horario.ui.fragments.dialogs.FeedbackDialog;
import com.artemchep.horario.ui.fragments.dialogs.HolidayModeDialog;
import com.artemchep.horario.ui.fragments.dialogs.LessonDialog;
import com.artemchep.horario.ui.fragments.dialogs.NotificationDialog;
import com.artemchep.horario.ui.fragments.dialogs.PaletteDialog;
import com.artemchep.horario.ui.fragments.dialogs.PrivacyPolicyDialog;
import com.artemchep.horario.ui.fragments.dialogs.SubjectDialog;
import com.artemchep.horario.ui.fragments.dialogs.SubjectGroupDialog;
import com.artemchep.horario.ui.fragments.dialogs.SubjectTaskDialog;
import com.artemchep.horario.ui.fragments.dialogs.TeacherDialog;
import com.artemchep.horario.ui.fragments.dialogs.TimetableDialog;
import com.artemchep.horario.ui.fragments.dialogs.WeekCycleDialog;
import com.artemchep.horario.ui.fragments.dialogs.base.DialogFragment;

/**
 * @author Artem Chepurnoy
 */
public class DialogHelper {

    private static final String TAG_FRAGMENT_PRIVACY = "dialog_privacy_policy";
    private static final String TAG_FRAGMENT_ABOUT = "dialog_about";
    private static final String TAG_FRAGMENT_FEEDBACK = "dialog_feedback";
    private static final String TAG_FRAGMENT_HOLIDAY_MODE = "dialog_holiday_mode";
    private static final String TAG_FRAGMENT_TIMETABLE = "dialog_timetable";
    private static final String TAG_FRAGMENT_SUBJECT = "dialog_subject";
    private static final String TAG_FRAGMENT_SUBJECT_TASK = "dialog_subject_task";
    private static final String TAG_FRAGMENT_SUBJECT_GROUP = "dialog_subject_group";
    private static final String TAG_FRAGMENT_SUBJECT_COLOR = "dialog_subject_color";
    private static final String TAG_FRAGMENT_NOTIFICATION = "dialog_notification";
    private static final String TAG_FRAGMENT_TEACHER = "dialog_teacher";
    private static final String TAG_FRAGMENT_LESSON = "dialog_lesson";
    private static final String TAG_FRAGMENT_EXAM = "dialog_exam";
    private static final String TAG_FRAGMENT_WEEK_CYCLE = "dialog_week_cycle";
    private static final String TAG_FRAGMENT_ABSENCE = "dialog_absence";

    public static void showPrivacyDialog(@NonNull AppCompatActivity activity) {
        showDialog(activity, new PrivacyPolicyDialog(), TAG_FRAGMENT_PRIVACY);
    }

    public static void showAboutDialog(@NonNull AppCompatActivity activity) {
        showAboutDialog(activity, AboutDialog.VIEW_ABOUT);
    }

    public static void showAboutDialog(@NonNull AppCompatActivity activity, String view) {
        Bundle args = new Bundle();
        args.putString(AboutDialog.EXTRA_VIEW, view);

        DialogFragment dialog = new AboutDialog();
        dialog.setArguments(args);
        showDialog(activity, dialog, TAG_FRAGMENT_ABOUT);
    }

    public static void showFeedbackDialog(@NonNull AppCompatActivity activity) {
        showDialog(activity, new FeedbackDialog(), TAG_FRAGMENT_FEEDBACK);
    }

    public static void showSubjectDialog(
            @NonNull AppCompatActivity activity,
            @NonNull String timetablePath,
            @Nullable Subject subject) {
        Bundle bundle = new Bundle();
        bundle.putString(SubjectDialog.EXTRA_USER, "YhTvBZ5eMTPeuhTKZAh9SeCiVGt1");
        bundle.putParcelable(SubjectDialog.EXTRA_SUBJECT_INFO, null);

        SubjectDialog dialog = new SubjectDialog();
        dialog.setArguments(bundle);
        showDialog(activity, dialog, TAG_FRAGMENT_SUBJECT);
    }

    public static void showSubjectTaskDialog(
            @NonNull AppCompatActivity activity,
            @NonNull String timetablePath,
            @Nullable SubjectTask subject,
            int type) {
        Bundle bundle = new Bundle();
        bundle.putString(SubjectTaskDialog.EXTRA_TIMETABLE_PATH, timetablePath);
        bundle.putParcelable(SubjectTaskDialog.EXTRA_SUBJECT, subject);
        bundle.putInt(SubjectTaskDialog.EXTRA_TYPE, type);

        SubjectTaskDialog dialog = new SubjectTaskDialog();
        dialog.setArguments(bundle);
        showDialog(activity, dialog, TAG_FRAGMENT_SUBJECT_TASK);
    }

    public static void showSubjectGroupDialog(
            @NonNull AppCompatActivity activity,
            @NonNull String timetablePath,
            @Nullable SubjectGroup group) {
        Bundle bundle = new Bundle();
        bundle.putString(SubjectGroupDialog.EXTRA_PATH, timetablePath);
        bundle.putParcelable(SubjectGroupDialog.EXTRA_GROUP, group);

        SubjectGroupDialog dialog = new SubjectGroupDialog();
        dialog.setArguments(bundle);
        showDialog(activity, dialog, TAG_FRAGMENT_SUBJECT_GROUP);
    }

    public static void showPaletteDialog(
            @NonNull AppCompatActivity activity,
            @NonNull String timetablePath, @Nullable String[] subjectIds,
            @Nullable Integer color) {
        Bundle bundle = new Bundle();
        bundle.putString(PaletteDialog.EXTRA_TIMETABLE_PATH, timetablePath);
        bundle.putStringArray(PaletteDialog.EXTRA_MODEL_ID_ARRAY, subjectIds);
        if (color != null) {
            bundle.putInt(PaletteDialog.EXTRA_COLOR, color);
            bundle.putBoolean(PaletteDialog.EXTRA_HAS_COLOR, true);
        }

        PaletteDialog dialog = new PaletteDialog();
        dialog.setArguments(bundle);
        showDialog(activity, dialog, TAG_FRAGMENT_SUBJECT_COLOR);
    }

    public static void showTeacherDialog(
            @NonNull AppCompatActivity activity,
            @NonNull String timetablePath,
            @Nullable Teacher teacher) {
        Bundle bundle = new Bundle();
        bundle.putString(TeacherDialog.EXTRA_TIMETABLE_PATH, timetablePath);
        bundle.putParcelable(TeacherDialog.EXTRA_TEACHER, teacher);

        TeacherDialog dialog = new TeacherDialog();
        dialog.setArguments(bundle);
        showDialog(activity, dialog, TAG_FRAGMENT_TEACHER);
    }

    public static void showNotificationDialog(
            @NonNull AppCompatActivity activity,
            @NonNull String timetablePath,
            @Nullable Notification notification) {
        Bundle bundle = new Bundle();
        bundle.putString(NotificationDialog.EXTRA_TIMETABLE_PATH, timetablePath);
        bundle.putParcelable(NotificationDialog.EXTRA_NOTIFICATION, notification);

        NotificationDialog dialog = new NotificationDialog();
        dialog.setArguments(bundle);
        showDialog(activity, dialog, TAG_FRAGMENT_NOTIFICATION);
    }

    public static void showExamDialog(
            @NonNull AppCompatActivity activity,
            @NonNull String timetablePath,
            @Nullable Exam exam,
            @Nullable Subject subject,
            @Nullable Teacher teacher) {
        Bundle bundle = new Bundle();
        bundle.putString(ExamDialog.EXTRA_TIMETABLE_PATH, timetablePath);
        bundle.putParcelable(ExamDialog.EXTRA_EXAM, exam);
        bundle.putParcelable(ExamDialog.EXTRA_SUBJECT, subject);
        bundle.putParcelable(ExamDialog.EXTRA_TEACHER, teacher);

        ExamDialog dialog = new ExamDialog();
        dialog.setArguments(bundle);
        showDialog(activity, dialog, TAG_FRAGMENT_EXAM);
    }

    public static void showAbsenceDialog(
            @NonNull AppCompatActivity activity,
            @NonNull String timetablePath,
            @NonNull String timetablePathPublic,
            @Nullable Absence absence,
            @Nullable Subject subject) {
        Bundle bundle = new Bundle();
        bundle.putString(AbsenceDialog.EXTRA_TIMETABLE_PATH, timetablePath);
        bundle.putString(AbsenceDialog.EXTRA_TIMETABLE_PATH_PUBLIC, timetablePathPublic);
        bundle.putParcelable(AbsenceDialog.EXTRA_ABSENCE, absence);
        bundle.putParcelable(AbsenceDialog.EXTRA_SUBJECT, subject);

        AbsenceDialog dialog = new AbsenceDialog();
        dialog.setArguments(bundle);
        showDialog(activity, dialog, TAG_FRAGMENT_ABSENCE);
    }

    public static void showWeekCycleDialog(
            @NonNull AppCompatActivity activity,
            @NonNull String timetablePath, int week) {
        Bundle bundle = new Bundle();
        bundle.putString(WeekCycleDialog.EXTRA_TIMETABLE_PATH, timetablePath);
        bundle.putInt(WeekCycleDialog.EXTRA_WEEK_CYCLE, week);

        WeekCycleDialog dialog = new WeekCycleDialog();
        dialog.setArguments(bundle);
        showDialog(activity, dialog, TAG_FRAGMENT_WEEK_CYCLE);
    }

    public static void showLessonDialog(
            @NonNull AppCompatActivity activity,
            @NonNull String timetablePath,
            @Nullable Lesson lesson,
            @Nullable Subject subject,
            @Nullable Teacher teacher) {
        Bundle bundle = new Bundle();
        bundle.putString(LessonDialog.EXTRA_TIMETABLE_PATH, timetablePath);
        bundle.putParcelable(LessonDialog.EXTRA_LESSON, lesson);
        bundle.putParcelable(LessonDialog.EXTRA_SUBJECT, subject);
        bundle.putParcelable(LessonDialog.EXTRA_TEACHER, teacher);

        LessonDialog dialog = new LessonDialog();
        dialog.setArguments(bundle);
        showDialog(activity, dialog, TAG_FRAGMENT_LESSON);
    }

    public static void showHolidayModeDialog(@NonNull AppCompatActivity activity) {
        showDialog(activity, new HolidayModeDialog(), TAG_FRAGMENT_HOLIDAY_MODE);
    }

    public static void showTimetableDialog(
            @NonNull AppCompatActivity activity,
            @NonNull String userId, @Nullable Timetable timetable) {
        Bundle bundle = new Bundle();
        bundle.putString(TimetableDialog.EXTRA_USER_ID, userId);
        bundle.putParcelable(TimetableDialog.EXTRA_TIMETABLE, timetable);

        TimetableDialog dialog = new TimetableDialog();
        dialog.setArguments(bundle);
        showDialog(activity, dialog, TAG_FRAGMENT_TIMETABLE);
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
