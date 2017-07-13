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
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSpinner;
import android.text.TextUtils;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.artemchep.basic.utils.HtmlUtils;
import com.artemchep.horario.R;
import com.artemchep.horario.models.Subject;
import com.artemchep.horario.models.SubjectTask;
import com.artemchep.horario.ui.FormatHelper;
import com.artemchep.horario.ui.fragments.dialogs.base.DialogFragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import jp.wasabeef.richeditor.RichEditor;

/**
 * Dialog for adding/editing existing
 * {@link Subject subject}.
 * You should put {@link #EXTRA_TIMETABLE_PATH}.
 *
 * @author Artem Chepurnoy
 */
public class SubjectTaskDialog extends DialogFragment {

    public static final String EXTRA_TIMETABLE_PATH = "extra::timetable_path";
    public static final String EXTRA_SUBJECT = "subject";
    public static final String EXTRA_TYPE = "type";

    private String mTimetablePath;
    private SubjectTask mSubject;

    private AppCompatEditText mTitleEditText;
    private AppCompatSpinner mPrioritySpinner;
    private RichEditor mRichEditor;
    private FormatHelper mFormatHelper;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedState) {
        Activity activity = getActivity();
        assert activity != null;

        Bundle args = getArguments();
        mTimetablePath = args.getString(EXTRA_TIMETABLE_PATH);

        // Load extras
        if (savedState != null) {
            // SubjectTask must not be null here!
            //noinspection ConstantConditions
            mSubject = savedState.getParcelable(EXTRA_SUBJECT);
        } else {
            SubjectTask subject = args.getParcelable(EXTRA_SUBJECT);
            if (subject != null) {
                mSubject = subject;
            } else {
                mSubject = new SubjectTask();
                mSubject.type = args.getInt(EXTRA_TYPE);
            }
        }

        // Load icon
        TypedArray a = getActivity().getTheme().obtainStyledAttributes(
                new int[]{R.attr.icon_label});
        int iconDrawableRes = a.getResourceId(0, 0);
        a.recycle();

        // Get title
        int titleRes;
        switch (mSubject.type) {
            case SubjectTask.TYPE_ANNOUNCEMENT:
                titleRes = TextUtils.isEmpty(mSubject.key)
                        ? R.string.dialog_announcement_new_title
                        : R.string.dialog_announcement_edit_title;
                break;
            case SubjectTask.TYPE_ASSIGNMENT:
                titleRes = TextUtils.isEmpty(mSubject.key)
                        ? R.string.dialog_assignment_new_title
                        : R.string.dialog_assignment_edit_title;
                break;
            case SubjectTask.TYPE_QUESTION:
            default:
                titleRes = TextUtils.isEmpty(mSubject.key)
                        ? R.string.dialog_question_new_title
                        : R.string.dialog_question_edit_title;
                break;
        }

        MaterialDialog md = new MaterialDialog.Builder(activity)
                .customView(R.layout.dialog_edit_subject_task, false)
                .iconRes(iconDrawableRes)
                .title(titleRes)
                .negativeText(android.R.string.cancel)
                .positiveText(android.R.string.ok)
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(
                            @NonNull MaterialDialog dialog,
                            @NonNull DialogAction which) {
                        switch (which) {
                            case POSITIVE:
                                if (TextUtils.isEmpty(getName())) {
                                    return; // don't dismiss dialog
                                }

                                updateCurrentSubject();

                                DatabaseReference subjectsRef = FirebaseDatabase.getInstance()
                                        .getReference(mTimetablePath);
                                //.child(Db.TASKS);
                                if (TextUtils.isEmpty(mSubject.key)) {
                                    // Create new subject
                                    DatabaseReference ref = subjectsRef.push();
                                    ref.setValue(mSubject);
                                    mSubject.key = ref.getKey();
                                } else subjectsRef.child(mSubject.key).setValue(mSubject);
                                break;
                        }
                        dismiss();
                    }
                })
                .autoDismiss(false)
                .build();

        View view = md.getCustomView();
        assert view != null;

        mTitleEditText = (AppCompatEditText) view.findViewById(R.id.title);
        mTitleEditText.setText(mSubject.title);
        mRichEditor = (RichEditor) view.findViewById(R.id.editor);
        mRichEditor.setHtml(mSubject.descriptionHtml);
        mPrioritySpinner = (AppCompatSpinner) view.findViewById(R.id.priority);

        mFormatHelper = null;//new FormatHelper();
        mFormatHelper.init(mRichEditor, view.findViewById(R.id.format));

        return md;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        updateCurrentSubject();
        outState.putParcelable(EXTRA_SUBJECT, mSubject);
    }

    private void updateCurrentSubject() {
        mSubject.title = getName();
        mSubject.descriptionHtml = mRichEditor.getHtml();
        mSubject.description = HtmlUtils.fromLegacyHtml(mSubject.descriptionHtml).toString();

        int priorityPos = mPrioritySpinner.getSelectedItemPosition();
        if (priorityPos == 0) {
            mSubject.priority = SubjectTask.PRIORITY_LOW;
        } else if (priorityPos == 1) {
            mSubject.priority = SubjectTask.PRIORITY_MEDIUM;
        } else mSubject.priority = SubjectTask.PRIORITY_HIGH;
    }

    /**
     * @return the name of the subject
     * @see Subject#name
     */
    @NonNull
    private String getName() {
        return mTitleEditText.getText().toString().trim();
    }

}
