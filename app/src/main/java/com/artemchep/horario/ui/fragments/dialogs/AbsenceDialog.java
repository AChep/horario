/*
 * Copyright (C) 2016 Artem Chepurnoy <artemchep@gmail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
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
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.artemchep.horario.R;
import com.artemchep.horario.database.Db;
import com.artemchep.horario.database.Persy;
import com.artemchep.horario.models.Absence;
import com.artemchep.horario.models.Subject;
import com.artemchep.horario.ui.DialogHelper;
import com.artemchep.horario.ui.UiHelper;
import com.artemchep.horario.ui.activities.MainActivity;
import com.artemchep.horario.ui.fragments.dialogs.base.DialogFragment;
import com.artemchep.horario.ui.fragments.master.SubjectsFragment;
import com.artemchep.horario.utils.DateUtilz;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import es.dmoral.toasty.Toasty;

/**
 * Dialog for adding/editing existing
 * {@link Absence absence}.
 * You should put {@link #EXTRA_TIMETABLE_PATH}.
 *
 * @author Artem Chepurnoy
 */
public class AbsenceDialog extends DialogFragment implements View.OnClickListener {

    public static final String EXTRA_TIMETABLE_PATH = "extra::timetable_path";
    public static final String EXTRA_TIMETABLE_PATH_PUBLIC = "extra::timetable_path::public";

    public static final String EXTRA_ABSENCE = "absence";
    public static final String EXTRA_SUBJECT = "subject";

    private String mTimetablePath;
    private String mTimetablePathPublic;

    private Absence mAbsence;
    private Subject mSubject;
    private String[] mMonths;

    private TextView mSubjectTextView;
    private TextView mDateTextView;
    private View mTimeClearView;
    private TextView mTimeTextView;
    private EditText mEditTextPlace;

    private ColorStateList mTextColorPrimary;
    private ColorStateList mTextColorHint;

    private Persy.Watcher<Subject> mWatcherSubjects;

    @NonNull
    private final ChildEventListener mSubjectListener = new ChildEventListener() {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Subject subject = mWatcherSubjects.getModel();
            updateTeacher(subject.key, subject);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Subject subject = mWatcherSubjects.getModel();
            updateTeacher(subject.key, subject);
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            String key = dataSnapshot.getKey();
            updateTeacher(key, null);
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }

        private void updateTeacher(@NonNull String key, @Nullable Subject subject) {
            if (key.equals(mAbsence.subject)) {
                setSubject(subject);
            }
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        TypedArray a = getActivity().getTheme().obtainStyledAttributes(
                new int[]{android.R.attr.textColorPrimary, android.R.attr.textColorHint});
        mTextColorPrimary = a.getColorStateList(0);
        mTextColorHint = a.getColorStateList(1);
        a.recycle();

        Bundle args = getArguments();
        assert args != null;
        mAbsence = args.getParcelable(EXTRA_ABSENCE);
        mTimetablePath = args.getString(EXTRA_TIMETABLE_PATH);
        mTimetablePathPublic = args.getString(EXTRA_TIMETABLE_PATH_PUBLIC);

        if (mAbsence == null) {
            mAbsence = new Absence();
        }

        MainActivity activity = (MainActivity) context;
        mWatcherSubjects = activity.getPersy().watchFor(Subject.class, mTimetablePathPublic + "/" + Db.SUBJECTS);

        mMonths = getResources().getStringArray(R.array.months);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedState) {
        // Load extras
        Subject subject;
        if (savedState != null) {
            // Those must not be null here!
            mAbsence = savedState.getParcelable(EXTRA_ABSENCE);
            subject = savedState.getParcelable(EXTRA_SUBJECT);
        } else {
            Bundle args = getArguments();
            assert args != null;
            subject = args.getParcelable(EXTRA_SUBJECT);
        }

        boolean isInEditMode = !TextUtils.isEmpty(mAbsence.key);

        Activity activity = getActivity();
        assert activity != null;
        MaterialDialog md = new MaterialDialog.Builder(activity)
                .customView(R.layout.dialog_edit_absence, false)
                .title(isInEditMode
                        ? R.string.dialog_absence_edit_title
                        : R.string.dialog_absence_new_title)
                .negativeText(android.R.string.cancel)
                .positiveText(isInEditMode
                        ? R.string.dialog_save
                        : R.string.dialog_add)
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(
                            @NonNull MaterialDialog dialog,
                            @NonNull DialogAction which) {
                        switch (which) {
                            case POSITIVE: {
                                Context context = getContext();
                                updateCurrentAbsence();

                                if (TextUtils.isEmpty(mAbsence.subject)) {
                                    Toasty.warning(context, "Subject must not be empty").show();
                                    mSubjectTextView.requestFocus();
                                    return;
                                }

                                if (mAbsence.date == 0) {
                                    Toasty.warning(context, "Date must not be empty").show();
                                    mDateTextView.requestFocus();
                                    return;
                                }

                                // Commit
                                DatabaseReference ref = FirebaseDatabase.getInstance()
                                        .getReference(mTimetablePath)
                                        .child(Db.ABSENCE);
                                if (TextUtils.isEmpty(mAbsence.key)) {
                                    // Create new lesson
                                    ref = ref.push();
                                    ref.setValue(mAbsence);
                                    mAbsence.key = ref.getKey();
                                } else ref.child(mAbsence.key).setValue(mAbsence);
                            }
                            case NEGATIVE:
                                dismiss();
                                break;
                        }
                    }
                })
                .autoDismiss(false)
                .build();

        View view = md.getCustomView();
        assert view != null;
        mDateTextView = (TextView) view.findViewById(R.id.date);
        view.findViewById(R.id.date_container).setOnClickListener(this);
        mTimeClearView = view.findViewById(R.id.time_clear);
        mTimeClearView.setOnClickListener(this);
        mTimeTextView = (TextView) view.findViewById(R.id.time);
        view.findViewById(R.id.time_container).setOnClickListener(this);
        View placeClearView = view.findViewById(R.id.place_clear);
        placeClearView.setOnClickListener(this);
        mEditTextPlace = (EditText) view.findViewById(R.id.place);
        mEditTextPlace.addTextChangedListener(new Watcher(placeClearView));
        mEditTextPlace.setText(mAbsence.reason);

        initSubject(view, subject);

        // pre initialize
        if (savedState == null) {
            if (TextUtils.isEmpty(mAbsence.subject)) setSubject(subject);
        }

        setDate(mAbsence.date);
        setTime(mAbsence.time);

        return md;
    }

    private void initSubject(@NonNull View view, @Nullable Subject subject) {
        View container = view.findViewById(R.id.subject_container);
        container.setOnClickListener(this);
        mSubjectTextView = (TextView) container.findViewById(R.id.subject);

        if (subject != null) {
            setSubject(subject);
        } else if (!TextUtils.isEmpty(mAbsence.subject)) {
            mSubjectTextView.setText(UiHelper.getPlaceholderText(mAbsence.subject));
        }
    }

    private void setSubject(@Nullable Subject subject) {
        mSubject = subject;

        if (subject != null) {
            mSubjectTextView.setTextColor(mTextColorPrimary);
            mSubjectTextView.setText(subject.name);
            mAbsence.subject = subject.key;
        } else {
            mSubjectTextView.setTextColor(mTextColorHint);
            mSubjectTextView.setText(getString(R.string.hint_subject));
            mAbsence.subject = null;
        }
    }

    private void setDate(int year, int month, int day) {
        setDate(DateUtilz.mergeDate(year, month, day));
    }

    private void setDate(int date) {
        mAbsence.date = date;

        if (date != 0) {
            int day = DateUtilz.getDay(date);
            int month = DateUtilz.getMonth(date);
            mDateTextView.setTextColor(mTextColorPrimary);
            mDateTextView.setText(mMonths[month] + " " + day);
        } else {
            mDateTextView.setTextColor(mTextColorHint);
            mDateTextView.setText(getString(R.string.hint_date));
        }
    }

    private void setTime(int hourOfDay, int minute) {
        setTime(hourOfDay * 60 + minute + 1);
    }

    private void setTime(int time) {
        mAbsence.time = time;

        if (time != 0) {
            mTimeTextView.setTextColor(mTextColorPrimary);
            mTimeTextView.setText(DateUtilz.formatLessonTime(time));
            mTimeClearView.setVisibility(View.VISIBLE);
        } else {
            mTimeTextView.setTextColor(mTextColorHint);
            mTimeTextView.setText(getString(R.string.hint_time));
            mTimeClearView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // Update current subject
        if (!TextUtils.isEmpty(mAbsence.subject)) {
            Subject subject = mWatcherSubjects.getMap().get(mAbsence.subject);
            if (subject != null) {
                setSubject(subject);
            }
        }

        mWatcherSubjects.addListener(mSubjectListener);
    }

    @Override
    public void onStop() {
        mWatcherSubjects.removeListener(mSubjectListener);
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        updateCurrentAbsence();
        outState.putParcelable(EXTRA_ABSENCE, mAbsence);
        outState.putParcelable(EXTRA_SUBJECT, mSubject);
    }

    private void updateCurrentAbsence() {
        mAbsence.reason = mEditTextPlace.getText().toString();
    }

    @Override
    public void onClick(@NonNull View view) {
        switch (view.getId()) {
            case R.id.subject_container:
                selectSubject(view);
                break;
            case R.id.date_container:
                selectDate(view);
                break;
            case R.id.time_container:
                selectTime(view);
                break;
            case R.id.time_clear:
                setTime(0);
                break;
            case R.id.place_clear:
                mEditTextPlace.setText(null);
                break;
        }
    }

    private void selectSubject(final @NonNull View v) {
        // Create standalone subjects list, so it won't
        // change mid-showing the popup
        final List<Subject> subjects = new ArrayList<>();
        subjects.addAll(mWatcherSubjects.getMap().values());
        Collections.sort(subjects, SubjectsFragment.createComparator());
        // Form popup menu and show it
        PopupMenu popup = new PopupMenu(getActivity(), v);
        popup.inflate(R.menu.dialog_exam_subjects);
        int i = 0;
        for (Subject s : subjects) popup.getMenu().add(R.id.subjects, i++, 0, s.name);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_create:
                        AppCompatActivity activity = (AppCompatActivity) getActivity();
                        DialogHelper.showSubjectDialog(activity, mTimetablePath, null);
                        break;
                    default:
                        Subject subject = subjects.get(item.getItemId());
                        subject = mWatcherSubjects.getMap().get(subject.key);
                        if (subject != null) {
                            setSubject(subject);
                        } else return false;
                        break;
                }
                return true;
            }
        });
        popup.show();
    }

    private void selectDate(final @NonNull View v) {
        int year, month, day;
        if (mAbsence.date != 0) {
            day = DateUtilz.getDay(mAbsence.date);
            month = DateUtilz.getMonth(mAbsence.date);
            year = DateUtilz.getYear(mAbsence.date);
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            day = calendar.get(Calendar.DAY_OF_MONTH);
            month = calendar.get(Calendar.MONTH);
            year = calendar.get(Calendar.YEAR);
        }
        DatePickerDialog tpd = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(
                    DatePicker view, int year, int monthOfYear,
                    int dayOfMonth) {
                setDate(year, monthOfYear, dayOfMonth);
            }
        }, year, month, day);
        tpd.show();
    }

    private void selectTime(final @NonNull View v) {
        int timeStart = Math.max(mAbsence.time - 1, 0);
        int m = timeStart % 60;
        int h = timeStart / 60;

        TimePickerDialog timepickerdialog = new TimePickerDialog(getActivity(),
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        setTime(hourOfDay, minute);
                    }
                }, h, m, true);
        timepickerdialog.show();
    }

    /**
     * @author Artem Chepurnoy
     */
    private static class Watcher implements TextWatcher {

        @NonNull
        private final View mView;

        Watcher(@NonNull View view) {
            mView = view;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            mView.setVisibility(TextUtils.isEmpty(editable)
                    ? View.GONE
                    : View.VISIBLE);
        }

    }

}
