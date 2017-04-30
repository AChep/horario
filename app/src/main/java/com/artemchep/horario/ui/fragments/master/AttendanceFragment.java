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
import com.artemchep.basic.utils.IntegerUtils;
import com.artemchep.horario.Palette;
import com.artemchep.horario.R;
import com.artemchep.horario.aggregator.Aggregator;
import com.artemchep.horario.database.Db;
import com.artemchep.horario.database.Persy;
import com.artemchep.horario.models.Absence;
import com.artemchep.horario.models.Subject;
import com.artemchep.horario.ui.DialogHelper;
import com.artemchep.horario.ui.activities.MainActivity;
import com.artemchep.horario.ui.adapters.AttendanceAdapter;
import com.artemchep.horario.ui.fragments.details.AbsenceDetailsFragment;
import com.artemchep.horario.ui.fragments.details.MooDetailsFragment;
import com.artemchep.horario.ui.widgets.CustomAppBar;
import com.artemchep.horario.utils.DateUtilz;
import com.bartoszlipinski.recyclerviewheader2.RecyclerViewHeader;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.artemchep.horario.ui.fragments.master.NotesFragment.EXTRA_TIMETABLE_PATH_PUBLIC;

/**
 * @author Artem Chepurnoy
 */
public class AttendanceFragment extends ModelFragment<Absence> {

    private static final String TAG = "AttendanceFragment";

    @NonNull
    public static Comparator<Absence> createComparator() {
        // sort by name and key
        return new Comparator<Absence>() {
            @Override
            public int compare(Absence o1, Absence o2) {
                int i = IntegerUtils.compare(o2.date, o1.date);
                if (i == 0) i = IntegerUtils.compare(o2.time, o1.time);
                return i != 0 ? i : o1.key.compareTo(o2.key);
            }
        };
    }

    private String mTimetablePathPublic;
    private String[] mDaysShort;
    private String[] mMonths;

    private BarChart mBarChart;

    private Persy.Watcher<Subject> mWatcherSubjects;

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
            List<Absence> list = getAdapter().getItems();
            for (int i = 0; i < list.size(); i++) {
                Absence absence = list.get(i);
                if (TextUtils.equals(absence.subject, key)) {
                    getAdapter().notifyItemChanged(i);
                }
            }
        }

    };

    @NonNull
    private final Aggregator.Observer<Absence> mObserver = new Aggregator.Observer<Absence>() {
        @Override
        public void add(@NonNull Absence model, int i) {
            avalanche();
        }

        @Override
        public void set(@NonNull Absence model, int i) {
            avalanche();
        }

        @Override
        public void remove(@NonNull Absence model, int i) {
            avalanche();
        }

        @Override
        public void move(@NonNull Absence model, int from, int to) {
            avalanche();
        }

        private int cutless(int date) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(DateUtilz.getYear(date),
                    DateUtilz.getMonth(date),
                    DateUtilz.getDay(date));
            int day = calendar.get(Calendar.DAY_OF_WEEK);

            // Change to our format (week starts from Monday:0)
            // from their format (week starts from Sunday:1).
            day -= 2;
            if (day < 0) day += 7;

            long time = calendar.getTimeInMillis() - day * 24 * 60 * 60 * 1000;
            calendar.setTimeInMillis(time);

            return DateUtilz.mergeDate(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
        }

        @Override
        public void avalanche() {
            if (mAggregator.getModels().isEmpty()) {
                return;
            }

            Calendar calendar = Calendar.getInstance();
            List<Absence> absences = mAggregator.getModels();

            int date = DateUtilz.mergeDate(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            int dateMax = date;
            int dateMin = Integer.MAX_VALUE;
            for (Absence absence : absences) {
                if (absence.date > dateMax)
                    dateMax = absence.date;
                if (absence.date < dateMin)
                    dateMin = absence.date;
            }

            List<BarDataSet> sets = new ArrayList<>();
            Comparator<BarEntry> comparator = new Comparator<BarEntry>() {
                @Override
                public int compare(BarEntry o1, BarEntry o2) {
                    return Float.compare(o1.getX(), o2.getX());
                }
            };

            int i = 0;
            int k = 0;
            date = DateUtilz.mergeDate(DateUtilz.getYear(dateMax), DateUtilz.getMonth(dateMax), 0);
            dateMin = DateUtilz.mergeDate(DateUtilz.getYear(dateMin), DateUtilz.getMonth(dateMin), 0);
            do {
                int j = 0;
                List<BarEntry> entries = new ArrayList<>();
                while (i < absences.size() && absences.get(i).date >= date) {
                    j++;
                    i++;
                }

                int year = DateUtilz.getYear(date);
                int month = DateUtilz.getMonth(date);
                entries.add(new BarEntry(k--, j));

                {
                    Collections.sort(entries, comparator);

                    BarDataSet set = new BarDataSet(entries, mMonths[month]);
                    set.setColor(Palette.PALETTE[month]);
                    sets.add(set);
                }

                // Go to previous month
                if (month == 0) {
                    year = year - 1;
                    month = 11;
                } else month--;

                date = DateUtilz.mergeDate(year, month, 0);
            } while (date >= dateMin);


            BarData barData = new BarData();
            for (BarDataSet set : sets) {
                barData.addDataSet(set);
            }
            barData.setValueFormatter(new IValueFormatter() {
                @Override
                public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                    return "" + (int) value;
                }
            });
            barData.setBarWidth(0.95f);
            mBarChart.setData(barData);
            mBarChart.setFitBars(true);
            mBarChart.notifyDataSetChanged();
            mBarChart.invalidate();
        }
    };

    @NonNull
    @Override
    protected Comparator<Absence> onCreateComparator() {
        return createComparator();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // Get timetable data
        Bundle args = getArguments();
        MainActivity activity = getMainActivity();
        mTimetablePathPublic = args.getString(EXTRA_TIMETABLE_PATH_PUBLIC);

        Persy persy = getMainActivity().getPersy();
        mWatcherSubjects = persy.watchFor(Subject.class, mTimetablePathPublic + "/" + Db.SUBJECTS);

        mAggregator.registerListener(mObserver);

        mDaysShort = getResources().getStringArray(R.array.days_short);
        mMonths = getResources().getStringArray(R.array.months);
    }

    @Override
    protected void setupToolbar() {
        super.setupToolbar();
        CustomAppBar appBar = getMainActivity().mAppBar;
        appBar.setTitle(getString(R.string.nav_attendance));
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
                DialogHelper.showAbsenceDialog(activity, mTimetablePath, mTimetablePathPublic, null, null);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_attendance, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedState) {
        super.onViewCreated(view, savedState);
        RecyclerView rv = (RecyclerView) view.findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));

        RecyclerViewHeader header = (RecyclerViewHeader) view.findViewById(R.id.header);
        header.attachTo(rv);

        mBarChart = (BarChart) header.findViewById(R.id.chart);
        mBarChart.getAxisLeft().setEnabled(false);
        mBarChart.getAxisRight().setEnabled(false);
        mBarChart.getXAxis().setEnabled(false);
        mBarChart.setHighlightFullBarEnabled(false);
        mBarChart.getDescription().setText("");
    }

    @Override
    public void onItemClick(@NonNull View view, @NonNull Absence item) {
        super.onItemClick(view, item);
        Bundle args = new Bundle();
        args.putBoolean(ModelFragment.EXTRA_EDITABLE, mEditable);
        args.putString(ModelFragment.EXTRA_TIMETABLE_PATH, mTimetablePath);
        args.putString(AbsenceDetailsFragment.EXTRA_TIMETABLE_PATH_PUBLIC, mTimetablePathPublic);
        args.putParcelable(MooDetailsFragment.EXTRA_MODEL, item);
        Fragment fragment = new AbsenceDetailsFragment();
        fragment.setArguments(args);

        MainActivity activity = (MainActivity) getActivity();
        activity.navigateDetailsFrame(fragment);
    }

    @NonNull
    @Override
    protected BaseAdapter<Absence> onCreateAdapter() {
        return new AttendanceAdapter(this, mAggregator.getModels(), mWatcherSubjects.getMap());
    }

    @Override
    public void onStart() {
        super.onStart();
        mWatcherSubjects.addListener(mWatcherSubjectsListener);
        getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        mWatcherSubjects.removeListener(mWatcherSubjectsListener);
        super.onStop();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mAggregator.unregisterListener(mObserver);
    }

    @NonNull
    @Override
    protected String getSnackBarRemoveMessage(List<Absence> list) {
//        if (list.size() == 1) {
//            Teacher teacher = list.get(0);
//            return getString(R.string.snackbar_teacher_removed_named, teacher.name);
//        } else return getString(R.string.snackbar_teacher_removed_plural);
        return "";
    }

    @NonNull
    @Override
    protected Class<Absence> getType() {
        return Absence.class;
    }

    @NonNull
    @Override
    protected String getPath() {
        return mTimetablePath + "/" + Db.ABSENCE;
    }

}
