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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.artemchep.horario.Palette;
import com.artemchep.horario.R;
import com.artemchep.horario.database.Db;
import com.artemchep.horario.database.Persy;
import com.artemchep.horario.models.Lesson;
import com.artemchep.horario.models.Subject;
import com.artemchep.horario.models.Teacher;
import com.artemchep.horario.ui.DialogHelper;
import com.artemchep.horario.ui.UiHelper;
import com.artemchep.horario.utils.DateUtilz;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;

import java.util.List;


/**
 * @author Artem Chepurnoy
 */
public class LessonDetailsFragment extends MooDetailsFragment<Lesson> {

    private static final String TAG = "LessonDetailsFragment";

    private View mInfoContainer;
    private TextView mDayTextView;
    private TextView mTimeTextView;
    private TextView mTypeTextView;
    private TextView mPlaceTextView;
    private TextView mTeacherTextView;
    private TextView mInfoTextView;

    private Persy.Watcher<Subject> mWatcherSubjects;
    private Persy.Watcher<Teacher> mWatcherTeachers;

    private String[] mDays;
    private String[] mTypes;

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
    protected Class<Lesson> getType() {
        return Lesson.class;
    }

    @NonNull
    @Override
    protected String getPath() {
        return mArgsTimetablePath + "/" + Db.LESSONS;
    }

    @Override
    public void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
        mDays = getResources().getStringArray(R.array.days);
        mTypes = getResources().getStringArray(R.array.lesson_types);

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
            @NonNull List<ContentItem<Lesson>> contentItems,
            @Nullable Bundle savedInstanceState) {
        getToolbar().inflateMenu(R.menu.details_lesson);

        ViewGroup vg = (ViewGroup) inflater.inflate(R.layout.fragment_details_lesson, container, false);
        initWithFab(R.id.action_edit, R.drawable.ic_pencil_white_24dp);

        mMenuEditSubject = getToolbar().getMenu().findItem(R.id.action_edit_subject);
        mMenuEditTeacher = getToolbar().getMenu().findItem(R.id.action_edit_teacher);

        mInfoContainer = vg.findViewById(R.id.info_container);
        mDayTextView = (TextView) vg.findViewById(R.id.day);
        mTimeTextView = (TextView) vg.findViewById(R.id.time);
        mTypeTextView = (TextView) vg.findViewById(R.id.type);
        mPlaceTextView = (TextView) vg.findViewById(R.id.place);
        mTeacherTextView = (TextView) vg.findViewById(R.id.teacher);
        mInfoTextView = (TextView) vg.findViewById(R.id.info);

        // Place
        contentItems.add(new TextContentItem<Lesson>(mPlaceTextView, mPlaceTextView) {
            @Override
            public String getText(Lesson model) {
                return model != null ? model.place : null;
            }
        });
        // Info
        contentItems.add(new TextContentItem<Lesson>(mInfoContainer, mInfoTextView) {
            @Override
            public String getText(Lesson model) {
                return model != null ? model.info : null;
            }
        });
        // Type
        contentItems.add(new ContentItem<Lesson>() {
            @Override
            public void onSet(@Nullable Lesson model) {
                if (model == null || model.type == 0) {
                    mTypeTextView.setVisibility(View.GONE);
                } else {
                    int index = Math.max(Math.min(model.type - 1, 3), 0);
                    mTypeTextView.setVisibility(View.VISIBLE);
                    mTypeTextView.setText(mTypes[index]);
                }
            }

            @Override
            public boolean hasChanged(@Nullable Lesson old, @Nullable Lesson model) {
                int a = old != null ? old.type : -1;
                int b = model != null ? model.type : -1;
                return a != b;
            }
        });
        // Time
        contentItems.add(new ContentItem<Lesson>() {
            @Override
            public void onSet(@Nullable Lesson model) {
                if (model == null) {
                    mTimeTextView.setVisibility(View.GONE);
                } else {
                    String text = DateUtilz.formatLessonTime(model.timeStart)
                            + " - " + DateUtilz.formatLessonTime(model.timeEnd);
                    mTimeTextView.setVisibility(View.VISIBLE);
                    mTimeTextView.setText(text);
                }
            }

            @Override
            public boolean hasChanged(@Nullable Lesson old, @Nullable Lesson model) {
                int a = old != null ? old.timeStart : -1;
                int b = model != null ? model.timeStart : -1;
                int c = old != null ? old.timeEnd : -1;
                int d = model != null ? model.timeEnd : -1;
                return a != b || c != d;
            }
        });
        // Day
        contentItems.add(new ContentItem<Lesson>() {
            @Override
            public void onSet(@Nullable Lesson model) {
                if (model == null) {
                    mDayTextView.setVisibility(View.GONE);
                } else {
                    int index = Math.max(Math.min(model.day - 1, 6), 0);
                    mDayTextView.setVisibility(View.VISIBLE);
                    mDayTextView.setText(mDays[index] + " - " + model.week);
                }
            }

            @Override
            public boolean hasChanged(@Nullable Lesson old, @Nullable Lesson model) {
                int a = old != null ? old.day : -1;
                int b = model != null ? model.day : -1;
                int c = old != null ? old.week : -1;
                int d = model != null ? model.week : -1;
                return a != b || c != d;
            }
        });
        // Teacher
        contentItems.add(new ContentItem<Lesson>() {
            @Override
            public void onSet(@Nullable Lesson model) {
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
            public boolean hasChanged(@Nullable Lesson old, @Nullable Lesson model) {
                return true;
            }
        });

        return vg;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                DialogHelper.showLessonDialog(getMainActivity(), mArgsTimetablePath, mModel,
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

        Lesson model = getModel();
        mMenuEditSubject.setVisible(model != null);
        mMenuEditTeacher.setVisible(model != null && model.teacher != null);
    }

    @Override
    protected void updateAppBar(@NonNull Lesson model) {
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
    protected boolean hasAdditionalInfo(@NonNull Lesson model) {
        /*
        * Always returns {@code true} because lesson should have
        * time/.. and those are displayed as additional info.
        */
        return true;
    }

}
