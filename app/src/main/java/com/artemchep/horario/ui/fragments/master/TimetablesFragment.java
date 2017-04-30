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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.artemchep.basic.ui.activities.ActivityBase;
import com.artemchep.horario.R;
import com.artemchep.horario.database.Db;
import com.artemchep.horario.models.Timetable;
import com.artemchep.horario.ui.DialogHelper;
import com.artemchep.horario.ui.activities.MainActivity;
import com.artemchep.horario.ui.adapters.TimetablesAdapter;
import com.artemchep.horario.ui.fragments.details.TimetableDetailsFragment;
import com.artemchep.horario.ui.widgets.CustomAppBar;

import java.util.Comparator;
import java.util.List;

/**
 * @author Artem Chepurnoy
 */
public class TimetablesFragment extends ModelFragment<Timetable> {

    public static final String EXTRA_USER_ID = "extra::user_id";

    @NonNull
    public static Comparator<Timetable> createComparator() {
        // sort by name and key
        return new Comparator<Timetable>() {
            @Override
            public int compare(Timetable o1, Timetable o2) {
                int i = o1.name.compareToIgnoreCase(o2.name);
                return i != 0 ? i : o1.key.compareTo(o2.key);
            }
        };
    }

    /**
     * The id of current user.
     * This value can not change.
     */
    private String mUserId;

    @NonNull
    @Override
    protected Comparator<Timetable> onCreateComparator() {
        return createComparator();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // Get arguments
        Bundle args = getArguments();
        mUserId = args.getString(EXTRA_USER_ID);
        mEditable = false;
    }

    @Override
    protected void setupToolbar() {
        super.setupToolbar();
        CustomAppBar appBar = getMainActivity().mAppBar;
        appBar.setTitle(getString(R.string.nav_profile_manage_timetable));
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
                DialogHelper.showTimetableDialog(activity, mUserId, null);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_timetables, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedState) {
        super.onViewCreated(view, savedState);
        RecyclerView rv = (RecyclerView) view.findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    @Override
    public void onItemClick(@NonNull View view, @NonNull Timetable item) {
        super.onItemClick(view, item);
        Bundle args = new Bundle();
        args.putString(TimetableDetailsFragment.EXTRA_USER_ID, mUserId);
        args.putParcelable(TimetableDetailsFragment.EXTRA_TIMETABLE, item);
        Fragment fragment = new TimetableDetailsFragment();
        fragment.setArguments(args);

        MainActivity activity = (MainActivity) getActivity();
        activity.navigateDetailsFrame(fragment);
    }

    @NonNull
    @Override
    protected BaseAdapter<Timetable> onCreateAdapter() {
        return new TimetablesAdapter(this, mAggregator.getModels());
    }

    @NonNull
    @Override
    protected String getSnackBarRemoveMessage(List<Timetable> list) {
        return "should-never-be-used";
    }

    @NonNull
    @Override
    protected Class<Timetable> getType() {
        return Timetable.class;
    }

    @NonNull
    @Override
    protected String getPath() {
        return mTimetablePath + "/" + Db.TIMETABLES;
    }

    @Override
    public void removeModels(@NonNull List<Timetable> list) {
        // Removing timetables is too complex: better
        // dodge.
    }

}
