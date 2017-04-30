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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.artemchep.horario.Palette;
import com.artemchep.horario.R;
import com.artemchep.horario.database.Db;
import com.artemchep.horario.database.Persy;
import com.artemchep.horario.models.Absence;
import com.artemchep.horario.models.Subject;
import com.artemchep.horario.ui.DialogHelper;
import com.artemchep.horario.ui.UiHelper;
import com.artemchep.horario.ui.activities.MainActivity;
import com.artemchep.horario.utils.DateUtilz;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;

import java.util.List;

/**
 * @author Artem Chepurnoy
 */
public class AbsenceDetailsFragment extends MooDetailsFragment<Absence> {

    public static final String EXTRA_TIMETABLE_PATH_PUBLIC = "extra::timetable_path::public";

    private static final String TAG = "AbsenceDetailsFragment";

    private View mInfoContainer;
    private TextView mDateTextView;
    private TextView mTimeTextView;
    private TextView mInfoTextView;

    private Persy.Watcher<Subject> mWatcherSubjects;

    private String mTimetablePathPublic;
    private String[] mMonths;

    private MenuItem mMenuEditSubject;

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
    @Override
    protected Class<Absence> getType() {
        return Absence.class;
    }

    @NonNull
    @Override
    protected String getPath() {
        return mArgsTimetablePath + "/" + Db.ABSENCE;
    }

    @Override
    public void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
        // Get timetable data
        Bundle args = getArguments();
        MainActivity activity = getMainActivity();
        mTimetablePathPublic = args.getString(EXTRA_TIMETABLE_PATH_PUBLIC);

        mMonths = getResources().getStringArray(R.array.months);

        Persy persy = getMainActivity().getPersy();
        mWatcherSubjects = persy.watchFor(Subject.class, mTimetablePathPublic + "/" + Db.SUBJECTS);
    }

    @Override
    public void onStart() {
        super.onStart();
        mWatcherSubjects.addListener(mWatcherSubjectsListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        mWatcherSubjects.removeListener(mWatcherSubjectsListener);
    }

    @Override
    protected ViewGroup onCreateContentView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @NonNull List<ContentItem<Absence>> contentItems,
            @Nullable Bundle savedInstanceState) {
        getToolbar().inflateMenu(R.menu.details_exam);

        ViewGroup vg = (ViewGroup) inflater.inflate(R.layout.fragment_details_absence, container, false);
        initWithFab(R.id.action_edit, R.drawable.ic_pencil_white_24dp);

        mMenuEditSubject = getToolbar().getMenu().findItem(R.id.action_edit_subject);

        mInfoContainer = vg.findViewById(R.id.info_container);
        mDateTextView = (TextView) vg.findViewById(R.id.date);
        mTimeTextView = (TextView) vg.findViewById(R.id.time);
        mInfoTextView = (TextView) vg.findViewById(R.id.info);

        // Info
        contentItems.add(new TextContentItem<Absence>(mInfoContainer, mInfoTextView) {
            @Override
            public String getText(Absence model) {
                return model != null ? model.reason : null;
            }
        });
        // Time
        contentItems.add(new ContentItem<Absence>() {
            @Override
            public void onSet(@Nullable Absence model) {
                if (model == null || model.time == 0) {
                    mTimeTextView.setVisibility(View.GONE);
                } else {
                    String text = DateUtilz.formatLessonTime(model.time);
                    mTimeTextView.setVisibility(View.VISIBLE);
                    mTimeTextView.setText(text);
                }
            }

            @Override
            public boolean hasChanged(@Nullable Absence old, @Nullable Absence model) {
                int a = old != null ? old.time : -1;
                int b = model != null ? model.time : -1;
                return a != b;
            }
        });
        // Date
        contentItems.add(new ContentItem<Absence>() {
            @Override
            public void onSet(@Nullable Absence model) {
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
            public boolean hasChanged(@Nullable Absence old, @Nullable Absence model) {
                int a = old != null ? old.date : -1;
                int b = model != null ? model.date : -1;
                return a != b;
            }
        });

        return vg;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit: {
                Subject subject = mWatcherSubjects.getMap().get(mModel.subject);
                DialogHelper.showAbsenceDialog(getMainActivity(), mArgsTimetablePath, mTimetablePathPublic, mModel, subject);
                break;
            }
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

        Absence model = getModel();
        mMenuEditSubject.setVisible(model != null);
    }

    @Override
    protected void updateAppBar(@NonNull Absence model) {
        super.updateAppBar(model);
        Subject subject = mWatcherSubjects.getMap().get(model.subject);
        if (subject == null) {
            getHelper().setTitle(UiHelper.TEXT_PLACEHOLDER);
            getHelper().setAppBarBackgroundColor(Palette.GREY);
        } else {
            getHelper().setTitle(subject.name + " (absence)");
            getHelper().setAppBarBackgroundColor(subject.color);
        }
    }

    @Override
    protected boolean hasAdditionalInfo(@NonNull Absence model) {
        /*
        * Always returns {@code true} because absence should have
        * date/.. and those are displayed as additional info.
        */
        return true;
    }

}
