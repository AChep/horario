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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.artemchep.basic.ui.activities.ActivityBase;
import com.artemchep.basic.utils.IntegerUtils;
import com.artemchep.horario.R;
import com.artemchep.horario.database.Db;
import com.artemchep.horario.database.Persy;
import com.artemchep.horario.models.Lesson;
import com.artemchep.horario.models.Subject;
import com.artemchep.horario.models.Teacher;
import com.artemchep.horario.ui.DialogHelper;
import com.artemchep.horario.ui.UiHelper;
import com.artemchep.horario.ui.activities.MainActivity;
import com.artemchep.horario.ui.fragments.dialogs.base.DialogFragment;
import com.artemchep.horario.ui.fragments.master.SubjectsFragment;
import com.artemchep.horario.ui.fragments.master.TeachersFragment;
import com.artemchep.horario.utils.DateUtilz;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shawnlin.numberpicker.NumberPicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import es.dmoral.toasty.Toasty;

/**
 * Dialog for adding/editing existing
 * {@link Lesson lesson}.
 *
 * @author Artem Chepurnoy
 */
public class LessonDialog extends DialogFragment implements View.OnClickListener {

    public static final String EXTRA_TIMETABLE_PATH = "extra::timetable_path";

    public static final String EXTRA_LESSON = "lesson";
    public static final String EXTRA_SUBJECT = "subject";
    public static final String EXTRA_TEACHER = "teacher";

    private String mTimetablePath;

    private Lesson mLesson;
    private Subject mSubject;
    private Teacher mTeacher;
    private String[] mDays;
    private String[] mTypes;

    private NumberPicker mWeekPicker;
    private TextView mSubjectTextView;
    private TextView mTeacherTextView;
    private View mTeacherClearView;
    private View mTypeClearView;
    private TextView mTypeTextView;
    private TextView mDayTextView;
    private EditText mEditTextPlace;
    private EditText mEditTextInfo;
    private Button mTimeFromButton;
    private Button mTimeToButton;

    private ColorStateList mTextColorPrimary;
    private ColorStateList mTextColorHint;

    private Persy.Watcher<Subject> mWatcherSubjects;
    private Persy.Watcher<Teacher> mWatcherTeachers;
    private Persy.Watcher<Lesson> mWatcherLessons;

    @NonNull
    private final ValueEventListener mWeekCycleListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            int weekCycle = dataSnapshot.exists()
                    ? dataSnapshot.getValue(int.class)
                    : -1;
            setWeekCycle(weekCycle);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };

    @NonNull
    private final ChildEventListener mSubjectListener = new Persy.ChildEventListenerAdapter() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Subject subject = mWatcherSubjects.getModel();
            updateSubject(subject.key, subject);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Subject subject = mWatcherSubjects.getModel();
            updateSubject(subject.key, subject);
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            String key = dataSnapshot.getKey();
            updateSubject(key, null);
        }

        private void updateSubject(@NonNull String key, @Nullable Subject subject) {
            if (key.equals(mLesson.subject)) {
                setSubject(subject);
            }
        }
    };

    @NonNull
    private final ChildEventListener mTeacherListener = new Persy.ChildEventListenerAdapter() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Teacher teacher = mWatcherTeachers.getModel();
            updateTeacher(teacher.key, teacher);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Teacher teacher = mWatcherTeachers.getModel();
            updateTeacher(teacher.key, teacher);
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            String key = dataSnapshot.getKey();
            updateTeacher(key, null);
        }

        private void updateTeacher(@NonNull String key, @Nullable Teacher teacher) {
            if (key.equals(mLesson.teacher)) {
                setTeacher(teacher);
            }
        }
    };

    @NonNull
    private final ChildEventListener mLessonListener = new Persy.ChildEventListenerAdapter();

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
        mLesson = args.getParcelable(EXTRA_LESSON);
        mTimetablePath = args.getString(EXTRA_TIMETABLE_PATH);

        if (mLesson == null) {
            mLesson = new Lesson();

            Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_WEEK);

            // Change to our format (week starts from Monday:0)
            // from their format (week starts from Sunday:1).
            day -= 2;
            if (day < 0) day += 7;
            mLesson.day = day;
        }

        MainActivity activity = (MainActivity) context;
        mWatcherSubjects = activity.getPersy().watchFor(Subject.class, mTimetablePath + "/" + Db.SUBJECTS);
        mWatcherTeachers = activity.getPersy().watchFor(Teacher.class, mTimetablePath + "/" + Db.TEACHERS);
        mWatcherLessons = activity.getPersy().watchFor(Lesson.class, mTimetablePath + "/" + Db.LESSONS);

        mTypes = getResources().getStringArray(R.array.lesson_types);
        mDays = getResources().getStringArray(R.array.days);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedState) {
        boolean isInEditMode = !TextUtils.isEmpty(mLesson.key);

        // Load extras
        Teacher teacher;
        Subject subject;
        if (savedState != null) {
            // Those must not be null here!
            mLesson = savedState.getParcelable(EXTRA_LESSON);
            teacher = savedState.getParcelable(EXTRA_TEACHER);
            subject = savedState.getParcelable(EXTRA_SUBJECT);
        } else {
            Bundle args = getArguments();
            assert args != null;
            teacher = args.getParcelable(EXTRA_TEACHER);
            subject = args.getParcelable(EXTRA_SUBJECT);
        }

        // Load icon
        TypedArray a = getActivity().getTheme().obtainStyledAttributes(
                new int[]{R.attr.icon_book_variant});
        int iconDrawableRes = a.getResourceId(0, 0);
        a.recycle();

        Activity activity = getActivity();
        assert activity != null;
        MaterialDialog md = new MaterialDialog.Builder(activity)
                .customView(R.layout.dialog_edit_lesson, false)
                .iconRes(iconDrawableRes)
                .title(isInEditMode
                        ? R.string.dialog_lesson_edit_title
                        : R.string.dialog_lesson_new_title)
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
                                updateCurrentLesson();

                                if (TextUtils.isEmpty(mLesson.subject)) {
                                    Toasty.warning(context, "Subject must not be empty").show();
                                    mSubjectTextView.requestFocus();
                                    return;
                                }

                                if (mLesson.timeStart == 0 || mLesson.timeEnd == 0) {
                                    Toasty.warning(context, "Time must not be empty").show();
                                    if (mLesson.timeStart == 0) {
                                        mTimeFromButton.requestFocus();
                                    } else mTimeToButton.requestFocus();
                                    return;
                                }

                                // Commit
                                DatabaseReference ref = FirebaseDatabase.getInstance()
                                        .getReference(mTimetablePath)
                                        .child(Db.LESSONS);
                                if (TextUtils.isEmpty(mLesson.key)) {
                                    // Create new lesson
                                    ref = ref.push();
                                    ref.setValue(mLesson);
                                    mLesson.key = ref.getKey();
                                } else ref.child(mLesson.key).setValue(mLesson);
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
        mDayTextView = (TextView) view.findViewById(R.id.day);
        view.findViewById(R.id.day_container).setOnClickListener(this);
        View placeClearView = view.findViewById(R.id.place_clear);
        placeClearView.setOnClickListener(this);
        mEditTextPlace = (EditText) view.findViewById(R.id.place);
        mEditTextPlace.addTextChangedListener(new Watcher(placeClearView));
        mEditTextPlace.setText(mLesson.place);
        View infoClearView = view.findViewById(R.id.info_clear);
        infoClearView.setOnClickListener(this);
        mEditTextInfo = (EditText) view.findViewById(R.id.info);
        mEditTextInfo.addTextChangedListener(new Watcher(infoClearView));
        mEditTextInfo.setText(mLesson.info);
        mTypeClearView = view.findViewById(R.id.type_clear);
        mTypeClearView.setOnClickListener(this);
        mTypeTextView = (TextView) view.findViewById(R.id.type);
        view.findViewById(R.id.type_container).setOnClickListener(this);
        mTimeFromButton = (Button) view.findViewById(R.id.from);
        mTimeFromButton.setOnClickListener(this);
        mTimeToButton = (Button) view.findViewById(R.id.to);
        mTimeToButton.setOnClickListener(this);

        initWeekNumberPicker(view);
        initSubject(view, subject);
        initTeacher(view, teacher);

        // pre initialize
        if (savedState == null) {
            if (TextUtils.isEmpty(mLesson.subject)) setSubject(subject);
            if (TextUtils.isEmpty(mLesson.teacher)) setTeacher(teacher);
        }

        setDay(mLesson.day);
        setType(mLesson.type);
        setTimeFrom(mLesson.timeStart);
        setTimeTo(mLesson.timeEnd);
        setWeekCycle(-1); // wait until we load this value from database

        return md;
    }

    private void initWeekNumberPicker(@NonNull View view) {
        view.findViewById(R.id.week_cycle).setOnClickListener(this);
        mWeekPicker = (NumberPicker) view.findViewById(R.id.week);
        mWeekPicker.setMinValue(0);
        mWeekPicker.setMaxValue(0);
    }

    private void setWeekCycle(int weekCycle) {
        int newMaxValue = Math.max(weekCycle, 0);
        int oldMaxValue = mWeekPicker.getMaxValue();
        mWeekPicker.setDisplayedValues(null);
        mWeekPicker.setMinValue(0);
        mWeekPicker.setMaxValue(newMaxValue);
        mWeekPicker.setEnabled(weekCycle >= 0);

        if (oldMaxValue < mLesson.week && weekCycle >= mLesson.week) {
            mWeekPicker.setValue(mLesson.week);
        }

        String[] s = new String[newMaxValue + 1];
        for (int i = 1; i < s.length; i++) s[i] = Integer.toString(i);
        s[0] = getString(R.string.dialog_lesson_day_week_all).toUpperCase();
        mWeekPicker.setDisplayedValues(s);
    }

    private void initSubject(@NonNull View view, @Nullable Subject subject) {
        View container = view.findViewById(R.id.subject_container);
        container.setOnClickListener(this);
        mSubjectTextView = (TextView) container.findViewById(R.id.subject);

        if (subject != null) {
            setSubject(subject);
        } else if (!TextUtils.isEmpty(mLesson.subject)) {
            mSubjectTextView.setText(UiHelper.getPlaceholderText(mLesson.subject));
        }
    }

    private void setSubject(@Nullable Subject subject) {
        mSubject = subject;

        if (subject != null) {
            mSubjectTextView.setTextColor(mTextColorPrimary);
            mSubjectTextView.setText(subject.name);
            mLesson.subject = subject.key;
        } else {
            mSubjectTextView.setTextColor(mTextColorHint);
            mSubjectTextView.setText(getString(R.string.hint_subject));
            mLesson.subject = null;
        }
    }

    private void initTeacher(@NonNull View view, @Nullable Teacher teacher) {
        View container = view.findViewById(R.id.teacher_container);
        container.setOnClickListener(this);
        mTeacherTextView = (TextView) container.findViewById(R.id.teacher);
        mTeacherClearView = container.findViewById(R.id.teacher_clear);
        mTeacherClearView.setOnClickListener(this);

        if (teacher != null) {
            setTeacher(teacher);
        } else if (!TextUtils.isEmpty(mLesson.teacher)) {
            mTeacherTextView.setText(UiHelper.getPlaceholderText(mLesson.teacher));
            mTeacherClearView.setVisibility(View.VISIBLE);
        }
    }

    private void setTeacher(@Nullable Teacher teacher) {
        mTeacher = teacher;

        if (teacher != null) {
            mTeacherClearView.setVisibility(View.VISIBLE);
            mTeacherTextView.setTextColor(mTextColorPrimary);
            mTeacherTextView.setText(teacher.name);
            mLesson.teacher = teacher.key;
        } else {
            mTeacherClearView.setVisibility(View.GONE);
            mTeacherTextView.setTextColor(mTextColorHint);
            mTeacherTextView.setText(getString(R.string.hint_teacher));
            mLesson.teacher = null;
        }
    }

    private void setType(int type) {
        mLesson.type = type;

        if (type != 0) {
            mTypeClearView.setVisibility(View.VISIBLE);
            mTypeTextView.setTextColor(mTextColorPrimary);
            mTypeTextView.setText(mTypes[type - 1]);
        } else {
            mTypeClearView.setVisibility(View.GONE);
            mTypeTextView.setTextColor(mTextColorHint);
            mTypeTextView.setText(getString(R.string.hint_type));
        }
    }

    private void setDay(int day) {
        mDayTextView.setText(mDays[day]);
        mLesson.day = day;
    }

    private void setTimeFrom(int timeFrom) {
        mLesson.timeStart = timeFrom;
        mTimeFromButton.setText(timeFrom != 0
                ? DateUtilz.formatLessonTime(timeFrom)
                : getString(R.string.dialog_lesson_time_from));
    }

    private void setTimeTo(int timeTo) {
        mLesson.timeEnd = timeTo;
        mTimeToButton.setText(timeTo != 0
                ? DateUtilz.formatLessonTime(timeTo)
                : getString(R.string.dialog_lesson_time_to));
    }

    @Override
    public void onStart() {
        super.onStart();

        // Update current subject
        if (!TextUtils.isEmpty(mLesson.subject)) {
            Subject subject = mWatcherSubjects.getMap().get(mLesson.subject);
            if (subject != null) {
                setSubject(subject);
            } // don't clear subject otherwise
        }

        // Update current teacher
        if (!TextUtils.isEmpty(mLesson.teacher)) {
            Teacher teacher = mWatcherTeachers.getMap().get(mLesson.teacher);
            if (teacher != null) {
                setTeacher(teacher);
            } // don't clear teacher otherwise
        }

        mWatcherSubjects.addListener(mSubjectListener);
        mWatcherTeachers.addListener(mTeacherListener);
        mWatcherLessons.addListener(mLessonListener);

        FirebaseDatabase.getInstance()
                .getReference(mTimetablePath)
                .child(Db.LEAF_WEEK_CYCLE)
                .addValueEventListener(mWeekCycleListener);
    }

    @Override
    public void onStop() {
        FirebaseDatabase.getInstance()
                .getReference(mTimetablePath)
                .child(Db.LEAF_WEEK_CYCLE)
                .removeEventListener(mWeekCycleListener);

        mWatcherSubjects.removeListener(mSubjectListener);
        mWatcherTeachers.removeListener(mTeacherListener);
        mWatcherLessons.removeListener(mLessonListener);
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        updateCurrentLesson();
        outState.putParcelable(EXTRA_LESSON, mLesson);
        outState.putParcelable(EXTRA_SUBJECT, mSubject);
        outState.putParcelable(EXTRA_TEACHER, mTeacher);
    }

    private void updateCurrentLesson() {
        mLesson.week = mWeekPicker.getValue();
        mLesson.place = mEditTextPlace.getText().toString();
        mLesson.info = mEditTextInfo.getText().toString();
    }

    @Override
    public void onClick(@NonNull View view) {
        switch (view.getId()) {
            case R.id.teacher_container:
                selectTeacher(view);
                break;
            case R.id.teacher_clear:
                setTeacher(null);
                break;
            case R.id.subject_container:
                selectSubject(view);
                break;
            case R.id.day_container:
                selectDay(view);
                break;
            case R.id.from:
                selectTimeFrom(view);
                break;
            case R.id.to:
                selectTimeTo(view);
                break;
            case R.id.type_container:
                selectType(view);
                break;
            case R.id.type_clear:
                setType(0);
                break;
            case R.id.place_clear:
                mEditTextPlace.setText(null);
                break;
            case R.id.info_clear:
                mEditTextInfo.setText(null);
                break;
            case R.id.week_cycle: {
                ActivityBase activity = (ActivityBase) getActivity();
                DialogHelper.showWeekCycleDialog(activity,
                        mTimetablePath, mWeekPicker.getMaxValue());
                break;
            }
        }
    }

    private void selectTeacher(final @NonNull View v) {
        // Create standalone teachers list, so it won't
        // change mid-showing the popup
        final List<Teacher> teachers = new ArrayList<>();
        teachers.addAll(mWatcherTeachers.getMap().values());
        Collections.sort(teachers, TeachersFragment.createComparator());
        // Form popup menu and show it
        PopupMenu popup = new PopupMenu(getActivity(), v);
        popup.inflate(R.menu.dialog_lesson_teachers);
        int i = 0;
        for (Teacher t : teachers) popup.getMenu().add(R.id.teachers, i++, 0, t.name);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_create:
                        AppCompatActivity activity = (AppCompatActivity) getActivity();
                        DialogHelper.showTeacherDialog(activity, mTimetablePath, null);
                        break;
                    default:
                        Teacher teacher = teachers.get(item.getItemId());
                        teacher = mWatcherTeachers.getMap().get(teacher.key);
                        if (teacher != null) {
                            setTeacher(teacher);
                        } else return false;
                        break;
                }
                return true;
            }
        });
        popup.show();
    }

    private void selectSubject(final @NonNull View v) {
        // Create standalone subjects list, so it won't
        // change mid-showing the popup
        final List<Subject> subjects = new ArrayList<>();
        subjects.addAll(mWatcherSubjects.getMap().values());
        Collections.sort(subjects, SubjectsFragment.createComparator());
        // Form popup menu and show it
        PopupMenu popup = new PopupMenu(getActivity(), v);
        popup.inflate(R.menu.dialog_lesson_subjects);
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

    private void selectDay(final @NonNull View v) {
        // Form popup menu and show it
        PopupMenu popup = new PopupMenu(getActivity(), v);
        int i = 0;
        for (String d : mDays) popup.getMenu().add(0, i++, 0, d);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                setDay(item.getItemId());
                return true;
            }
        });
        popup.show();
    }

    private void selectTimeFrom(final @NonNull View v) {
        List<Integer> list = new ArrayList<>();
        for (Lesson lesson : mWatcherLessons.getMap().values()) {
            final int time = lesson.timeStart;
            if (mLesson.timeEnd != 0
                    && mLesson.timeEnd < time
                    || list.contains(time)) {
                continue;
            }

            list.add(time);
        }

        // If there's no suggested times then just show
        // the time-picker.
        if (list.isEmpty()) {
            selectTimeFromPicker(v);
            return;
        }

        // Sort from smallest to biggest
        Collections.sort(list, new Comparator<Integer>() {
            @Override
            public int compare(Integer a, Integer b) {
                return IntegerUtils.compare(a, b);
            }
        });

        PopupMenu popup = new PopupMenu(getActivity(), v);
        for (Integer time : list) popup.getMenu().add(0, time, 0, DateUtilz.formatLessonTime(time));
        popup.getMenu().add(0, 0, 0, getString(R.string.dialog_lesson_pick_time));
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int time = item.getItemId();
                switch (time) {
                    case 0:
                        selectTimeFromPicker(v);
                        break;
                    default:
                        setTimeFrom(time);
                }
                return true;
            }
        });
        popup.show();
    }

    private void selectTimeFromPicker(final @NonNull View v) {
        int timeStart = Math.max(mLesson.timeStart - 1, 0);
        int m = timeStart % 60;
        int h = timeStart / 60;

        TimePickerDialog timepickerdialog = new TimePickerDialog(getActivity(),
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        int time = hourOfDay * 60 + minute + 1;
                        if (mLesson.timeEnd != 0 && mLesson.timeEnd < time) {
                            String msg = getString(R.string.dialog_lesson_time_error_invalid_time);
                            Toasty.warning(getContext(), msg).show();
                        } else setTimeFrom(time);
                    }
                }, h, m, true);
        timepickerdialog.show();
    }

    private void selectTimeTo(final @NonNull View v) {
        List<Integer> list = new ArrayList<>();
        for (Lesson lesson : mWatcherLessons.getMap().values()) {
            final int time = lesson.timeEnd;
            if (mLesson.timeStart != 0
                    && mLesson.timeStart > time
                    || list.contains(time)) {
                continue;
            }

            list.add(time);
        }

        // If there's no suggested times then just show
        // the time-picker.
        if (list.isEmpty()) {
            selectTimeToPicker(v);
            return;
        }

        // Sort from smallest to biggest
        Collections.sort(list, new Comparator<Integer>() {
            @Override
            public int compare(Integer a, Integer b) {
                return IntegerUtils.compare(a, b);
            }
        });

        PopupMenu popup = new PopupMenu(getActivity(), v);
        for (Integer time : list) popup.getMenu().add(0, time, 0, DateUtilz.formatLessonTime(time));
        popup.getMenu().add(0, 0, 0, getString(R.string.dialog_lesson_pick_time));
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int time = item.getItemId();
                switch (time) {
                    case 0:
                        selectTimeToPicker(v);
                        break;
                    default:
                        setTimeTo(time);
                }
                return true;
            }
        });
        popup.show();
    }

    private void selectTimeToPicker(final @NonNull View v) {
        int timeStart = Math.max(mLesson.timeEnd - 1, 0);
        int m = timeStart % 60;
        int h = timeStart / 60;

        TimePickerDialog timepickerdialog = new TimePickerDialog(getActivity(),
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        int time = hourOfDay * 60 + minute + 1;
                        if (mLesson.timeStart != 0 && mLesson.timeStart > time) {
                            String msg = getString(R.string.dialog_lesson_time_error_invalid_time);
                            Toasty.warning(getContext(), msg).show();
                        } else setTimeTo(time);
                    }
                }, h, m, true);
        timepickerdialog.show();
    }

    private void selectType(final @NonNull View v) {
        // Form popup menu and show it
        PopupMenu popup = new PopupMenu(getActivity(), v);
        int i = 1;
        for (String d : mTypes) popup.getMenu().add(0, i++, 0, d);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                setType(item.getItemId());
                return true;
            }
        });
        popup.show();
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
