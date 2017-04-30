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
package com.artemchep.horario.ui.fragments.master;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.artemchep.basic.ui.activities.ActivityBase;
import com.artemchep.horario.R;
import com.artemchep.horario.database.Db;
import com.artemchep.horario.database.Persy;
import com.artemchep.horario.models.Exam;
import com.artemchep.horario.models.Subject;
import com.artemchep.horario.models.Teacher;
import com.artemchep.horario.ui.DialogHelper;
import com.artemchep.horario.ui.activities.MainActivity;
import com.artemchep.horario.ui.adapters.ExamsAdapter;
import com.artemchep.horario.ui.fragments.details.ExamDetailsFragment;
import com.artemchep.horario.ui.fragments.details.MooDetailsFragment;
import com.artemchep.horario.ui.widgets.CustomAppBar;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;

import java.util.Comparator;
import java.util.List;

/**
 * @author Artem Chepurnoy
 */
public class ExamsFragment extends ModelFragment<Exam> {

    @NonNull
    public static Comparator<Exam> createComparator() {
        return new Comparator<Exam>() {
            @Override
            public int compare(Exam o1, Exam o2) {
                int i = o1.date - o2.date;
                if (i == 0) i = o1.time - o2.time;
                if (i == 0) i = o1.key.compareTo(o2.key);
                return i;
            }
        };
    }

    private Persy.Watcher<Subject> mWatcherSubjects;
    private Persy.Watcher<Teacher> mWatcherTeachers;

    @NonNull
    private final ChildEventListener mWatcherSubjectsListener = new Persy.ChildEventListenerAdapter() {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Subject subject = mWatcherSubjects.getModel();
            notifyAdapterItemsBy(subject.key);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Subject subject = mWatcherSubjects.getModel();
            notifyAdapterItemsBy(subject.key);
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            notifyAdapterItemsBy(dataSnapshot.getKey());
        }

        private void notifyAdapterItemsBy(String key) {
            List<Exam> list = getAdapter().getItems();
            for (int i = 0; i < list.size(); i++) {
                Exam exam = list.get(i);
                if (TextUtils.equals(exam.subject, key)) {
                    getAdapter().notifyItemChanged(i);
                }
            }
        }

    };

    @NonNull
    private final ChildEventListener mWatcherTeachersListener = new Persy.ChildEventListenerAdapter() {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Teacher teacher = mWatcherTeachers.getModel();
            notifyAdapterItemsBy(teacher.key);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Teacher teacher = mWatcherTeachers.getModel();
            notifyAdapterItemsBy(teacher.key);
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            notifyAdapterItemsBy(dataSnapshot.getKey());
        }

        private void notifyAdapterItemsBy(String key) {
            List<Exam> list = getAdapter().getItems();
            for (int i = 0; i < list.size(); i++) {
                Exam exam = list.get(i);
                if (TextUtils.equals(exam.teacher, key)) {
                    getAdapter().notifyItemChanged(i);
                }
            }
        }

    };

    @NonNull
    @Override
    protected Comparator<Exam> onCreateComparator() {
        return createComparator();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Persy persy = getMainActivity().getPersy();
        mWatcherSubjects = persy.watchFor(Subject.class, mTimetablePath + "/" + Db.SUBJECTS);
        mWatcherTeachers = persy.watchFor(Teacher.class, mTimetablePath + "/" + Db.TEACHERS);
    }

    @Override
    protected void setupToolbar() {
        super.setupToolbar();
        CustomAppBar appBar = getMainActivity().mAppBar;
        appBar.setTitle(getString(R.string.nav_exams));
    }

    @Override
    protected void setupFab() {
        super.setupFab();
        FloatingActionButton fab = getMainActivity().mFab;
        fab.show();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityBase activity = getMainActivity();
                DialogHelper.showExamDialog(activity, mTimetablePath, null, null, null);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_exams, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedState) {
        super.onViewCreated(view, savedState);
        RecyclerView rv = (RecyclerView) view.findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    @Override
    public void onItemClick(@NonNull View view, @NonNull Exam item) {
        super.onItemClick(view, item);
        Bundle args = new Bundle();
        args.putBoolean(EXTRA_EDITABLE, mEditable);
        args.putString(ModelFragment.EXTRA_TIMETABLE_PATH, mTimetablePath);
        args.putParcelable(MooDetailsFragment.EXTRA_MODEL, item);
        Fragment fragment = new ExamDetailsFragment();
        fragment.setArguments(args);

        MainActivity activity = (MainActivity) getActivity();
        activity.navigateDetailsFrame(fragment);
    }

    @NonNull
    @Override
    protected BaseAdapter<Exam> onCreateAdapter() {
        return new ExamsAdapter(this, mAggregator.getModels(),
                mWatcherSubjects.getMap(),
                mWatcherTeachers.getMap());
    }

    @Override
    public void onStart() {
        super.onStart();
        mWatcherSubjects.addListener(mWatcherSubjectsListener);
        mWatcherTeachers.addListener(mWatcherTeachersListener);
        getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        mWatcherSubjects.removeListener(mWatcherSubjectsListener);
        mWatcherTeachers.removeListener(mWatcherTeachersListener);
        super.onStop();
    }

    @NonNull
    @Override
    protected String getSnackBarRemoveMessage(List<Exam> list) {
        if (list.size() == 1) {
            Exam exam = list.get(0);
            Subject subject = mWatcherSubjects.getMap().get(exam.subject);

            if (subject != null) {
                return getString(R.string.snackbar_exam_removed_named, subject.name);
            } else return getString(R.string.snackbar_exam_removed);
        } else return getString(R.string.snackbar_exam_removed_plural);
    }

    @NonNull
    @Override
    protected Class<Exam> getType() {
        return Exam.class;
    }

    @NonNull
    @Override
    protected String getPath() {
        return mTimetablePath + "/" + Db.EXAMS;
    }

}
