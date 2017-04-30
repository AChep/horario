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
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.transition.TransitionManager;
import android.support.v13.view.ViewCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialcab.MaterialCab;
import com.artemchep.basic.ui.MultiSelector;
import com.artemchep.basic.ui.drawables.TextDrawable;
import com.artemchep.horario.R;
import com.artemchep.horario.aggregator.Aggregator;
import com.artemchep.horario.aggregator.Filter;
import com.artemchep.horario.database.Db;
import com.artemchep.horario.database.Persy;
import com.artemchep.horario.models.Lesson;
import com.artemchep.horario.models.Subject;
import com.artemchep.horario.models.Teacher;
import com.artemchep.horario.ui.DialogHelper;
import com.artemchep.horario.ui.activities.MainActivity;
import com.artemchep.horario.ui.fragments.details.LessonDetailsFragment;
import com.artemchep.horario.ui.fragments.details.MooDetailsFragment;
import com.artemchep.horario.ui.views.WeekView;
import com.artemchep.horario.ui.widgets.ContainersLayout;
import com.artemchep.horario.ui.widgets.CustomAppBar;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @author Artem Chepurnoy
 */
public class LessonsFragment extends MasterFragment implements MultiSelector.Callback<String>, Toolbar.OnMenuItemClickListener, MaterialCab.Callback {

    public static final String EXTRA_TIMETABLE_PATH = "extra::timetable_path";

    private static final String SIS_SELECTIONS = "sis_selections";

    private boolean mEditable;
    private String mTimetablePath;

    private LayerDrawable mWeekCompositeDrawable;
    private TextDrawable mWeekDrawable;
    private View mEmptyView;
    private WeekView mWeekView;
    private MenuItem mWeekMenuItem;
    private int mWeekCycle = 1;

    @NonNull
    private final HashSet<String> mHiddenSet = new HashSet<>();

    private Persy.Watcher<Lesson> mWatcherLessons;
    private Persy.Watcher<Subject> mWatcherSubjects;
    private Persy.Watcher<Teacher> mWatcherTeachers;

    @NonNull
    private final ChildEventListener mLessonEventListener = new Persy.ChildEventListenerAdapter() {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            mVirgin = false;
            // TransitionManager.beginDelayedTransition(mWeekView, mTransition);
            Lesson lesson = mWatcherLessons.getModel();
            mWeekView.getFilterManager().put(lesson);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            mVirgin = false;
            // TransitionManager.beginDelayedTransition(mWeekView, mTransition);
            Lesson lesson = mWatcherLessons.getModel();
            mWeekView.getFilterManager().put(lesson);
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            mVirgin = false;
            super.onChildRemoved(dataSnapshot);
            // TransitionManager.beginDelayedTransition(mWeekView, mTransition);
            Lesson data = dataSnapshot.getValue(Lesson.class);
            data.key = dataSnapshot.getKey();
            mWeekView.getFilterManager().remove(data);
            // we must remove it from actual filter after removing
            // it from manager.
            mHiddenSet.remove(data.key);
        }

    };

    /**
     * Notifies {@link #mWeekView} to update when subjects
     * do change.
     */
    @NonNull
    private final ChildEventListener mSubjectEventListener = new Persy.ChildEventListenerAdapter() {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            String key = dataSnapshot.getKey();
            notifySubjectChanged(key);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            String key = dataSnapshot.getKey();
            notifySubjectChanged(key);
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            String key = dataSnapshot.getKey();
            notifySubjectChanged(key);
        }

        private void notifySubjectChanged(@NonNull String subjectKey) {
            for (Lesson lesson : mWeekView.getFilterManager().getModels()) {
                if (subjectKey.equals(lesson.subject)) {
                    if (ViewCompat.isLaidOut(mWeekView)) {
                        TransitionManager.beginDelayedTransition(mWeekView);
                    }

                    mWeekView.notifyLessonChanged(lesson);
                }
            }
        }

    };

    /**
     * Notifies {@link #mWeekView} to update when teachers
     * do change.
     */
    @NonNull
    private final ChildEventListener mTeacherEventListener = new Persy.ChildEventListenerAdapter() {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            String key = dataSnapshot.getKey();
            notifyTeacherChanged(key);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            String key = dataSnapshot.getKey();
            notifyTeacherChanged(key);
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            String key = dataSnapshot.getKey();
            notifyTeacherChanged(key);
        }

        private void notifyTeacherChanged(@NonNull String teacherKey) {
            for (Lesson lesson : mWeekView.getFilterManager().getModels()) {
                if (teacherKey.equals(lesson.teacher)) {
                    if (ViewCompat.isLaidOut(mWeekView)) {
                        TransitionManager.beginDelayedTransition(mWeekView);
                    }

                    mWeekView.notifyLessonChanged(lesson);
                }
            }
        }

    };

    @NonNull
    private final ValueEventListener mWeekCycleEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            int weekCycle = dataSnapshot.exists()
                    ? dataSnapshot.getValue(int.class)
                    : 1;
            setWeekCycle(weekCycle);
            registerAllWatchersListeners();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };

    /**
     * {@code true} if we haven't got any lessons from database yet,
     * {@code false} otherwise.
     */
    private boolean mVirgin = true;
    private boolean mRegistered;

    @NonNull
    private final Aggregator.Observer<Lesson> mObserver = new Aggregator.Observer<Lesson>() {

        @Override
        public void add(@NonNull Lesson model, int i) {
            refreshEmptyView();
        }

        @Override
        public void set(@NonNull Lesson model, int i) {
        }

        @Override
        public void remove(@NonNull Lesson model, int i) {
            refreshEmptyView();
        }

        @Override
        public void move(@NonNull Lesson model, int from, int to) {
        }

        @Override
        public void avalanche() {
            refreshEmptyView();
        }

        private void refreshEmptyView() {
            final boolean empty = mWeekView.getFilterManager().getModels().isEmpty();
            mEmptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
            mWeekView.setVisibility(empty ? View.GONE : View.VISIBLE);
        }

    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // Get timetable data
        Bundle args = getArguments();
        mTimetablePath = args.getString(EXTRA_TIMETABLE_PATH);
        mEditable = args.getBoolean(ModelFragment.EXTRA_EDITABLE);

        Persy persy = getMainActivity().getPersy();
        mWatcherLessons = persy.watchFor(Lesson.class, mTimetablePath + "/" + Db.LESSONS);
        mWatcherSubjects = persy.watchFor(Subject.class, mTimetablePath + "/" + Db.SUBJECTS);
        mWatcherTeachers = persy.watchFor(Teacher.class, mTimetablePath + "/" + Db.TEACHERS);
    }

    @Override
    protected void setupToolbar() {
        super.setupToolbar();
        CustomAppBar appBar = getMainActivity().mAppBar;
        appBar.setTitle(getString(R.string.nav_lessons));

        Toolbar toolbar = appBar.getToolbarSpecific();
        toolbar.inflateMenu(R.menu.master_lessons_week);
        toolbar.setOnMenuItemClickListener(this);

        // Load week icon
        TypedArray a = getActivity().getTheme().obtainStyledAttributes(
                new int[]{R.attr.icon_calendar_empty_grey, R.attr.is_dark_theme});
        int iconDrawableRes = a.getResourceId(0, 0);
        boolean isDarkTheme = a.getBoolean(1, false);
        a.recycle();

        float density = getResources().getDisplayMetrics().density;
        mWeekDrawable = new TextDrawable();
        mWeekDrawable.setTranslationY(density * 2);
        mWeekDrawable.setTextSize(density * 10);
        mWeekDrawable.setColor(isDarkTheme ? Color.WHITE : Color.GRAY);
        mWeekCompositeDrawable = new LayerDrawable(new Drawable[]{
                ContextCompat.getDrawable(getContext(), iconDrawableRes),
                mWeekDrawable
        });

        Menu menu = toolbar.getMenu();
        mWeekMenuItem = menu.findItem(R.id.action_switch_week);
        mWeekMenuItem.setIcon(mWeekCompositeDrawable);
        mWeekMenuItem.setVisible(mWeekCycle > 1);
        updateWeekNumberMenuItemText();
    }

    @Override
    protected void setupFab() {
        super.setupFab();
        FloatingActionButton fab = getMainActivity().mFab;
        fab.hide(); // use toolbar instead
    }

    @Override
    protected void setupContainers() {
        super.setupContainers();
        ContainersLayout containers = getMainActivity().mContainers;
        containers.setCardDecorationEnabled(false);
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_lessons, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEmptyView = view.findViewById(R.id.empty);
        mWeekView = (WeekView) view.findViewById(R.id.week_view);
        mWeekView.setMultiSelectEnabled(mEditable);
        mWeekView.getFilterManager().registerListener(mObserver);

        if (savedInstanceState != null) {
            // Restore current selections; note: do it before registering
            // event listener -- all other components should have restored
            // their states.
            ArrayList<String> selections = savedInstanceState.getStringArrayList(SIS_SELECTIONS);
            if (selections != null) for (String key : selections) {
                mWeekView.getMultiSelector().add(key);
            }
        }

        mWeekView.setSubjects(mWatcherSubjects.getMap());
        mWeekView.setTeachers(mWatcherTeachers.getMap());
        mWeekView.setFilter(new Filter<Lesson>() {
            @Override
            public boolean isValid(@NonNull Lesson model) {
                return !mHiddenSet.contains(model.key);
            }
        });
        mWeekView.getMultiSelector().registerListener(this);
        mWeekView.setOnLessonClickListener(new WeekView.OnLessonClickListener() {
            @Override
            public void onLessonClick(@NonNull View view, @NonNull Lesson lesson) {
                Bundle args = new Bundle();
                args.putString(ModelFragment.EXTRA_TIMETABLE_PATH, mTimetablePath);
                args.putParcelable(MooDetailsFragment.EXTRA_MODEL, lesson);
                Fragment fragment = new LessonDetailsFragment();
                fragment.setArguments(args);

                MainActivity activity = (MainActivity) getActivity();
                activity.navigateDetailsFrame(fragment);
            }
        });
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_lesson: {
                AppCompatActivity activity = (AppCompatActivity) getActivity();
                DialogHelper.showLessonDialog(activity, mTimetablePath, null, null, null);
                break;
            }
            case R.id.action_switch_week: {
                if (ViewCompat.isLaidOut(mWeekView)) {
                    // TransitionManager.beginDelayedTransition(mWeekView, mTransition);
                }

                int week = mWeekView.getWeekNumber() % mWeekCycle;
                mWeekView.setWeekNumber(week + 1);
                updateWeekNumberMenuItemText();
                break;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseDatabase.getInstance()
                .getReference(mTimetablePath)
                .child(Db.LEAF_WEEK_CYCLE)
                .addValueEventListener(mWeekCycleEventListener);

        //noinspection StatementWithEmptyBody
        if (mVirgin) {
            // We should ensure that week cycle is received before
            // receiving lessons/subjects/teachers to be able to
            // show current week on start-up.
        } else registerAllWatchersListeners();
    }

    private void registerAllWatchersListeners() {
        if (mRegistered) {
            return;
        } else mRegistered = true;

        mWatcherSubjects.addListener(mSubjectEventListener);
        mWatcherTeachers.addListener(mTeacherEventListener);
        mWatcherLessons.addListener(mLessonEventListener);

        // Since lessons map could have changed without our
        // supervision we should apply possible changes now.
        Collection<Lesson> list = mWatcherLessons.getMap().values();
        mWeekView.getFilterManager().replaceAll(list);
    }

    @Override
    public void onStop() {
        super.onStop();
        FirebaseDatabase.getInstance()
                .getReference(mTimetablePath)
                .child(Db.LEAF_WEEK_CYCLE)
                .removeEventListener(mWeekCycleEventListener);

        if (mRegistered) {
            mRegistered = false;

            mWatcherSubjects.removeListener(mSubjectEventListener);
            mWatcherTeachers.removeListener(mTeacherEventListener);
            mWatcherLessons.removeListener(mLessonEventListener);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putStringArrayList(SIS_SELECTIONS, mWeekView.getMultiSelector().getSelections());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        mWeekView.getFilterManager().unregisterListener(mObserver);
        mWeekView.getMultiSelector().unregisterListener(this);
        super.onDestroyView();
    }

    // --------------------------
    // -- WEEK CYCLE ------------
    // --------------------------

    private void setWeekCycle(int weekCycle) {
        mWeekCycle = Math.max(weekCycle, 1);

        if (mVirgin) {
            Calendar calendar = Calendar.getInstance();
            int woy = calendar.get(Calendar.WEEK_OF_YEAR);
            int week = (woy % mWeekCycle) + 1;
            mWeekView.setWeekNumber(week);
        }

        if (mWeekMenuItem != null) { // it can be called before creating menu
            mWeekMenuItem.setVisible(mWeekCycle > 1);
            updateWeekNumberMenuItemText();
        }
    }

    /**
     * Updates little text indicator inside of the week drawable
     * to {@link WeekView#getWeekNumber() current week number}.
     */
    private void updateWeekNumberMenuItemText() {
        final int weekNumber = mWeekView.getWeekNumber();
        mWeekDrawable.setText(Integer.toString(weekNumber));
    }

    // --------------------------
    // -- ACTION MODE -----------
    // --------------------------

    @Override
    public void onSelectorStatusChanged(@NonNull MultiSelector<String> selector, boolean isEmpty) {
        if (isEmpty) {
            getMainActivity().finishContextualActionBar();
        } else getMainActivity().startContextualActionBar(this);
    }

    @Override
    public void onSelectorSelectionsChanged(@NonNull MultiSelector<String> selector) {
        int count = selector.getSelections().size();
        getMainActivity().getContextualActionBar().setTitle("" + count);
    }

    @Override
    public boolean onCabCreated(MaterialCab cab, Menu menu) {
        cab.setMenu(R.menu.master_context);
        return true;
    }

    @Override
    public boolean onCabItemClicked(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_delete:
                // Build items list.
                List<Lesson> list = new ArrayList<>();
                for (String key : mWeekView.getMultiSelector().getSelections()) {
                    List<Lesson> models = mWeekView.getFilterManager().getModels();
                    for (int i = 0; i < models.size(); i++) {
                        final Lesson item = models.get(i);
                        if (item.key.equals(key)) {
                            list.add(item);
                        }
                    }
                }

                removeModels(list);
                break;
            default:
                return false;
        }
        getMainActivity().finishContextualActionBar();
        return true;
    }

    @Override
    public boolean onCabFinished(MaterialCab cab) {
        mWeekView.getMultiSelector().clear();
        return true;
    }

    public void removeModels(final @NonNull List<Lesson> list) {
        View view = getView();
        if (view == null) {
            return;
        }

        // Create snack-bar message
        String message;
        if (list.size() == 1) {
            Lesson exam = list.get(0);
            Subject subject = mWatcherSubjects.getMap().get(exam.subject);

            if (subject != null) {
                message = getString(R.string.snackbar_exam_removed_named, subject.name);
            } else message = getString(R.string.snackbar_exam_removed);
        } else message = getString(R.string.snackbar_exam_removed_plural);

        // Show snack-bar
        View coordinator = getMainActivity().mContainers.findViewById(R.id.coordinator_layout_fab);
        Snackbar.make(coordinator != null ? coordinator : view, message, Snackbar.LENGTH_LONG)
                .addCallback(new Snackbar.Callback() {
                    @Override
                    public void onShown(Snackbar sb) {
                        super.onShown(sb);
                        if (ViewCompat.isLaidOut(mWeekView)) {
                            TransitionManager.beginDelayedTransition(mWeekView);
                        }

                        for (Lesson lesson : list) mHiddenSet.add(lesson.key);
                        mWeekView.getFilterManager().refilter();
                    }

                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        super.onDismissed(transientBottomBar, event);
                        if (event == DISMISS_EVENT_ACTION) {
                            if (ViewCompat.isLaidOut(mWeekView)) {
                                TransitionManager.beginDelayedTransition(mWeekView);
                            }

                            for (Lesson lesson : list) mHiddenSet.remove(lesson.key);
                            mWeekView.getFilterManager().refilter();
                            return;
                        }

                        Map<String, Object> childUpdates = new HashMap<>();
                        for (Lesson lesson : list) childUpdates.put(lesson.key, null);
                        mWatcherLessons.getDatabase().updateChildren(childUpdates);
                    }
                })
                .setAction(R.string.snackbar_undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                })
                .show();
    }

}
