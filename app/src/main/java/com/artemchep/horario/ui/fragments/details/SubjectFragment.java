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

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.artemchep.horario.Palette;
import com.artemchep.horario.R;
import com.artemchep.horario.models.SubjectTask;
import com.artemchep.horario.ui.DialogHelper;
import com.artemchep.horario.ui.adapters.SubjectsTasksAdapter;
import com.artemchep.horario.ui.fragments.master.ModelFragment;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import java.util.Comparator;
import java.util.List;

/**
 * @author Artem Chepurnoy
 */
public class SubjectFragment extends DetailsFragment implements ViewPager.OnPageChangeListener {

    private static final String TAG = "SubjectFragment";

    private DetailsHelper mHelper = new DetailsHelper();

    private View.OnClickListener mTaskOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int type;
            switch (v.getId()) {
                case R.id.fab_menu_announcement:
                    type = SubjectTask.TYPE_ANNOUNCEMENT;
                    break;
                case R.id.fab_menu_assignment:
                    type = SubjectTask.TYPE_ASSIGNMENT;
                    break;
                case R.id.fab_menu_question:
                default:
                    type = SubjectTask.TYPE_QUESTION;
                    break;
            }

            DialogHelper.showSubjectTaskDialog(getMainActivity(),
                    "user/YhTvBZ5eMTPeuhTKZAh9SeCiVGt1/timetable_public/-KguZ6-bx-bkMGjutdkJ/subjects/-KbyZXLDGApiHnpxiHKW/tasks",
                    null, type);
        }
    };

    private FloatingActionButton mFab;
    private FloatingActionMenu mFabStream;

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_details_subject_social, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mHelper.init(getMainActivity(), view);
        mHelper.setAppBarBackgroundColor(Palette.PURPLE);
        mHelper.setTitle("Testing subjects");

        mFab = (FloatingActionButton) view.findViewById(R.id.fab);
        mFabStream = (FloatingActionMenu) view.findViewById(R.id.fab_menu);
        mFabStream.findViewById(R.id.fab_menu_announcement).setOnClickListener(mTaskOnClickListener);
        mFabStream.findViewById(R.id.fab_menu_assignment).setOnClickListener(mTaskOnClickListener);
        mFabStream.findViewById(R.id.fab_menu_question).setOnClickListener(mTaskOnClickListener);

        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getChildFragmentManager(), FragmentPagerItems.with(getContext())
                .add("Stream", StreamFragment.class)
                .add("Materials", PageFragment.class)
                .add("About", AboutFragment.class)
                .create());

        ViewPager viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(this);

        SmartTabLayout viewPagerTab = (SmartTabLayout) view.findViewById(R.id.viewpagertab);
        viewPagerTab.setViewPager(viewPager);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        if (position == 0) { // stream
            mFab.setVisibility(View.GONE);
            mFabStream.setVisibility(View.VISIBLE);
        } else if (position == 1) { // material
            mFab.show(true);
            mFab.setVisibility(View.VISIBLE);
            mFabStream.close(false);
            mFabStream.setVisibility(View.GONE);
        } else {
            mFab.hide(true);
            mFabStream.close(true);
            mFabStream.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    /**
     * @author Artem Chepurnoy
     */
    public static class PageFragment extends Fragment {

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            NestedScrollView scrollView = new NestedScrollView(getContext());
            scrollView.addView(new FrameLayout(getContext()), new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2000));
            return scrollView;
        }
    }

    /**
     * @author Artem Chepurnoy
     */
    public static class StreamFragment extends ModelFragment<SubjectTask> {

        @Override
        public void onAttach(final Context context) {
            super.onAttach(context);
            // disable editing because model fragment needs
            // an activity for that.
            mEditable = false;
        }

        @NonNull
        @Override
        protected Comparator<SubjectTask> onCreateComparator() {
            return new Comparator<SubjectTask>() {
                @Override
                public int compare(SubjectTask o1, SubjectTask o2) {
                    return o1.key.compareTo(o2.key);
                }
            };
        }

        @Nullable
        @Override
        public View onCreateView(
                LayoutInflater inflater, @Nullable ViewGroup container,
                @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_details_subject_stream, container, false);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedState) {
            super.onViewCreated(view, savedState);
            RecyclerView rv = (RecyclerView) view.findViewById(R.id.recycler);
            rv.setLayoutManager(new LinearLayoutManager(getActivity()));
        }

        @NonNull
        @Override
        protected String getSnackBarRemoveMessage(List<SubjectTask> list) {
            return "sdf";
        }

        @NonNull
        @Override
        protected BaseAdapter<SubjectTask> onCreateAdapter() {
            return new SubjectsTasksAdapter(this, mAggregator.getModels());
        }

        @NonNull
        @Override
        protected Class<SubjectTask> getType() {
            return SubjectTask.class;
        }

        @NonNull
        @Override
        protected String getPath() {
            return "user/YhTvBZ5eMTPeuhTKZAh9SeCiVGt1/timetable_public/-KguZ6-bx-bkMGjutdkJ/subjects/-KbyZXLDGApiHnpxiHKW/tasks";
        }

        // --------------------------
        // -- DONT MODIFY ACTIVITY --
        // --------------------------

        @Override
        protected void setupFab() {
        }

        @Override
        protected void setupToolbar() {
        }

        @Override
        protected void setupContainers() {
        }

    }

    /**
     * @author Artem Chepurnoy
     */
    public static class AboutFragment extends Fragment {

        @Nullable
        @Override
        public View onCreateView(
                LayoutInflater inflater, @Nullable ViewGroup container,
                @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_details_subject_about, container, false);
        }

    }

    /**
     * @author Artem Chepurnoy
     */
    public static class StudentsFragment extends ModelFragment<SubjectTask> {

        @Override
        public void onAttach(final Context context) {
            super.onAttach(context);
            // disable editing because model fragment needs
            // an activity for that.
            mEditable = false;
        }

        @NonNull
        @Override
        protected Comparator<SubjectTask> onCreateComparator() {
            return new Comparator<SubjectTask>() {
                @Override
                public int compare(SubjectTask o1, SubjectTask o2) {
                    return o1.key.compareTo(o2.key);
                }
            };
        }

        @Nullable
        @Override
        public View onCreateView(
                LayoutInflater inflater, @Nullable ViewGroup container,
                @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_details_subject_stream, container, false);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedState) {
            super.onViewCreated(view, savedState);
            RecyclerView rv = (RecyclerView) view.findViewById(R.id.recycler);
            rv.setLayoutManager(new LinearLayoutManager(getActivity()));
        }

        @NonNull
        @Override
        protected String getSnackBarRemoveMessage(List<SubjectTask> list) {
            return "sdf";
        }

        @NonNull
        @Override
        protected BaseAdapter<SubjectTask> onCreateAdapter() {
            return new SubjectsTasksAdapter(this, mAggregator.getModels());
        }

        @NonNull
        @Override
        protected Class<SubjectTask> getType() {
            return SubjectTask.class;
        }

        @NonNull
        @Override
        protected String getPath() {
            return "user/YhTvBZ5eMTPeuhTKZAh9SeCiVGt1/timetable_public/-KguZ6-bx-bkMGjutdkJ/subjects/-KbyZXLDGApiHnpxiHKW/students";
        }

        // --------------------------
        // -- DONT MODIFY ACTIVITY --
        // --------------------------

        @Override
        protected void setupFab() {
        }

        @Override
        protected void setupToolbar() {
        }

        @Override
        protected void setupContainers() {
        }

    }

}
