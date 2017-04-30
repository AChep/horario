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
package com.artemchep.horario.ui.fragments.details;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.artemchep.horario.Palette;
import com.artemchep.horario.R;
import com.artemchep.horario.database.Db;
import com.artemchep.horario.database.Persy;
import com.artemchep.horario.models.Exam;
import com.artemchep.horario.models.Lesson;
import com.artemchep.horario.models.Subject;
import com.artemchep.horario.models.Teacher;
import com.artemchep.horario.ui.DialogHelper;
import com.artemchep.horario.ui.UiHelper;
import com.artemchep.horario.ui.fragments.master.ModelFragment;
import com.artemchep.horario.utils.DateUtilz;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

/**
 * @author Artem Chepurnoy
 */
public class ExamDetailsFragment extends MooDetailsFragment<Exam> {

    private static final String TAG = "ExamDetailsFragment";

    private View mInfoContainer;
    private TextView mDateTextView;
    private TextView mTimeTextView;
    private TextView mPlaceTextView;
    private TextView mTeacherTextView;
    private TextView mInfoTextView;

    private Persy.Watcher<Subject> mWatcherSubjects;
    private Persy.Watcher<Teacher> mWatcherTeachers;

    private String[] mMonths;

    private MenuItem mMenuEditSubject;
    private MenuItem mMenuEditTeacher;

    @NonNull
    private final ChildEventListener mWatcherSubjectsListener = new Persy.ChildEventListenerAdapter() {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            onAny(dataSnapshot);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            onAny(dataSnapshot);
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            onAny(dataSnapshot);
        }

        private void onAny(DataSnapshot dataSnapshot) {
            if (mModel != null && dataSnapshot.getKey().equals(mModel.subject)) {
                updateAll();
            }
        }
    };

    @NonNull
    private final ChildEventListener mWatcherTeachersListener = new Persy.ChildEventListenerAdapter() {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            onAny(dataSnapshot);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            onAny(dataSnapshot);
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            onAny(dataSnapshot);
        }

        private void onAny(DataSnapshot dataSnapshot) {
            if (mModel != null && dataSnapshot.getKey().equals(mModel.teacher)) {
                updateAll();
            }
        }
    };

    @NonNull
    @Override
    protected Class<Exam> getType() {
        return Exam.class;
    }

    @NonNull
    @Override
    protected String getPath() {
        return mArgsTimetablePath + "/" + Db.EXAMS;
    }

    @Override
    public void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
        mMonths = getResources().getStringArray(R.array.months);

        Persy persy = getMainActivity().getPersy();
        mWatcherSubjects = persy.watchFor(Subject.class, mArgsTimetablePath + "/" + Db.SUBJECTS);
        mWatcherTeachers = persy.watchFor(Teacher.class, mArgsTimetablePath + "/" + Db.TEACHERS);
    }

    @Override
    public void onStart() {
        super.onStart();
        mWatcherSubjects.addListener(mWatcherSubjectsListener);
        mWatcherTeachers.addListener(mWatcherTeachersListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        mWatcherSubjects.removeListener(mWatcherSubjectsListener);
        mWatcherTeachers.removeListener(mWatcherTeachersListener);
    }

    @Override
    protected ViewGroup onCreateContentView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @NonNull List<ContentItem<Exam>> contentItems,
            @Nullable Bundle savedInstanceState) {
        getToolbar().inflateMenu(R.menu.details_exam);

        ViewGroup vg = (ViewGroup) inflater.inflate(R.layout.fragment_details_exam, container, false);
        initWithFab(R.id.action_edit, R.drawable.ic_pencil_white_24dp);

        mMenuEditSubject = getToolbar().getMenu().findItem(R.id.action_edit_subject);
        mMenuEditTeacher = getToolbar().getMenu().findItem(R.id.action_edit_teacher);

        mInfoContainer = vg.findViewById(R.id.info_container);
        mDateTextView = (TextView) vg.findViewById(R.id.date);
        mTimeTextView = (TextView) vg.findViewById(R.id.time);
        mPlaceTextView = (TextView) vg.findViewById(R.id.place);
        mTeacherTextView = (TextView) vg.findViewById(R.id.teacher);
        mInfoTextView = (TextView) vg.findViewById(R.id.info);

        // Place
        contentItems.add(new TextContentItem<Exam>(mPlaceTextView, mPlaceTextView) {
            @Override
            public String getText(Exam model) {
                return model != null ? model.place : null;
            }
        });
        // Info
        contentItems.add(new TextContentItem<Exam>(mInfoContainer, mInfoTextView) {
            @Override
            public String getText(Exam model) {
                return model != null ? model.info : null;
            }
        });
        // Time
        contentItems.add(new ContentItem<Exam>() {
            @Override
            public void onSet(@Nullable Exam model) {
                if (model == null || model.time == 0) {
                    mTimeTextView.setVisibility(View.GONE);
                } else {
                    String text = DateUtilz.formatLessonTime(model.time);
                    mTimeTextView.setVisibility(View.VISIBLE);
                    mTimeTextView.setText(text);
                }
            }

            @Override
            public boolean hasChanged(@Nullable Exam old, @Nullable Exam model) {
                int a = old != null ? old.time : -1;
                int b = model != null ? model.time : -1;
                return a != b;
            }
        });
        // Date
        contentItems.add(new ContentItem<Exam>() {
            @Override
            public void onSet(@Nullable Exam model) {
                if (model == null) {
                    mDateTextView.setVisibility(View.GONE);
                } else {
                    int day = DateUtilz.getDay(model.date);
                    int month = Math.max(Math.min(DateUtilz.getMonth(model.date), 11), 0);
                    mDateTextView.setVisibility(View.VISIBLE);
                    mDateTextView.setText(mMonths[month] + " " + day);
                }
            }

            @Override
            public boolean hasChanged(@Nullable Exam old, @Nullable Exam model) {
                int a = old != null ? old.date : -1;
                int b = model != null ? model.date : -1;
                return a != b;
            }
        });
        // Teacher
        contentItems.add(new ContentItem<Exam>() {
            @Override
            public void onSet(@Nullable Exam model) {
                if (model == null || TextUtils.isEmpty(model.teacher)) {
                    mTeacherTextView.setVisibility(View.GONE);
                } else {
                    Teacher teacher = mWatcherTeachers.getMap().get(model.teacher);
                    String text = !TextUtils.isEmpty(model.teacher)
                            ? teacher != null ? teacher.name : UiHelper.TEXT_PLACEHOLDER
                            : null;
                    mTeacherTextView.setVisibility(View.VISIBLE);
                    mTeacherTextView.setText(text);
                }
            }

            @Override
            public boolean hasChanged(@Nullable Exam old, @Nullable Exam model) {
                return true;
            }
        });

        return vg;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                DialogHelper.showExamDialog(getMainActivity(), mArgsTimetablePath, mModel,
                        mWatcherSubjects.getMap().get(mModel.subject),
                        mWatcherTeachers.getMap().get(mModel.teacher));
                break;
            case R.id.action_edit_teacher:
                if (mModel.teacher != null) {
                    Teacher teacher = mWatcherTeachers.getMap().get(mModel.teacher);
                    if (teacher != null) {
                        DialogHelper.showTeacherDialog(getMainActivity(), mArgsTimetablePath, teacher);
                    }
                }
                break;
            case R.id.action_edit_subject:
                if (mModel.subject != null) {
                    Subject subject = mWatcherSubjects.getMap().get(mModel.subject);
                    if (subject != null) {
                        DialogHelper.showSubjectDialog(getMainActivity(), mArgsTimetablePath, subject);
                    }
                }
                break;
            case R.id.action_delete:
                mDatabaseRef.setValue(null);
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    protected void updateAll() {
        super.updateAll();

        Exam model = getModel();
        mMenuEditSubject.setVisible(model != null);
        mMenuEditTeacher.setVisible(model != null && model.teacher != null);
    }

    @Override
    protected void updateAppBar(@NonNull Exam model) {
        super.updateAppBar(model);
        Subject subject = mWatcherSubjects.getMap().get(model.subject);
        if (subject == null) {
            getHelper().setTitle(UiHelper.TEXT_PLACEHOLDER);
            getHelper().setAppBarBackgroundColor(Palette.GREY);
        } else {
            getHelper().setTitle(subject.name + " (lesson)");
            getHelper().setAppBarBackgroundColor(subject.color);
        }
    }

    @Override
    protected boolean hasAdditionalInfo(@NonNull Exam model) {
        /*
        * Always returns {@code true} because exam should have
        * date/.. and those are displayed as additional info.
        */
        return true;
    }

}
