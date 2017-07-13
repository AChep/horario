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
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.artemchep.horario.Palette;
import com.artemchep.horario.R;
import com.artemchep.horario.database.Persy;
import com.artemchep.horario.database.models.SubjectTask;
import com.artemchep.horario.ui.activities.ActivityHorario;
import com.artemchep.horario._new.fragments.FragmentBase;
import com.artemchep.horario.ui.fragments.master.SubjectAboutFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;
import com.thebluealliance.spectrum.internal.ColorUtil;

/**
 * @author Artem Chepurnoy
 */
public class SubjectTaskDetailsFragment extends FragmentBase implements ViewPager.OnPageChangeListener {

    public static final String EXTRA_COLOR = "extra::decor::color";

    public static final String EXTRA_SUBJECT_ID = "extra::subject_id";
    public static final String EXTRA_GROUP_ID = "extra::group_id";
    public static final String EXTRA_USER_ID = "extra::user_id";
    public static final String EXTRA_TASK_ID = "extra::task_id";
    public static final String EXTRA_TASK = "extra::task_model";

    private static final String STATE_TASK_INFO = "state::task_info";

    private Toolbar mToolbar;
    private AppBarLayout mAppBar;
    private SmartTabLayout mSmartTabLayout;
    private ViewPager mViewPager;

    @ColorInt
    private int mDecorColor;

    private Persy.Watcher<SubjectTask> mWatcher;
    private ValueEventListener mValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                SubjectTask task = dataSnapshot.getValue(SubjectTask.class);
                bind(task);
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };

    private String mSubjectId;
    private String mGroupId;
    private String mTaskId;
    private String mUserId;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Bundle args = getArguments();
        mDecorColor = args.getInt(EXTRA_COLOR, Palette.GREY);
        mSubjectId = args.getString(EXTRA_SUBJECT_ID);
        mGroupId = args.getString(EXTRA_GROUP_ID);
        mTaskId = args.getString(EXTRA_TASK_ID);

        ActivityHorario activity = (ActivityHorario) getActivity();
        mWatcher = activity.getPersy().watchFor(SubjectTask.class,
                "_subjects_/" + mSubjectId + "/groups/" + mGroupId +
                        "/tasks/" + mTaskId);
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_subject_task_details, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedState) {
        super.onViewCreated(view, savedState);

        mAppBar = (AppBarLayout) view.findViewById(R.id.appbar);
        mToolbar = (Toolbar) mAppBar.findViewById(R.id.toolbar);
        mSmartTabLayout = (SmartTabLayout) mAppBar.findViewById(R.id.tabs);
        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);

        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getChildFragmentManager(), FragmentPagerItems.with(getContext())
                .add("Instructions", F.class)
                .add("Student work", F.class)
                .create()) {

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                Object item = super.instantiateItem(container, position);
                if (item instanceof SubjectAboutFragment) {
                    SubjectAboutFragment f = (SubjectAboutFragment) item;
                    //f.setSubject(mSubject);
                }
                return item;
            }

        };

        mViewPager.setAdapter(adapter);
        mViewPager.addOnPageChangeListener(this);

        setupDecorColor(mDecorColor);
    }

    private void setupDecorColor(int color) {
        color |= 0xFF000000; // ignore alpha bits
        final boolean isColorDark = ColorUtil.isColorDark(color);

        Drawable overflowIcon;
        if (isColorDark) {
            mToolbar.setTitleTextColor(Color.WHITE);
            mToolbar.setNavigationIcon(R.drawable.ic_arrow_left_white_24dp);
            overflowIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_dots_vertical_white_24dp);
            mSmartTabLayout.setDefaultTabTextColor(Color.WHITE);
            mSmartTabLayout.setSelectedIndicatorColors(Color.WHITE);
        } else {
            mToolbar.setTitleTextColor(Color.BLACK);
            mToolbar.setNavigationIcon(R.drawable.ic_arrow_left_black_24dp);
            overflowIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_dots_vertical_black_24dp);
            mSmartTabLayout.setDefaultTabTextColor(Color.BLACK);
            mSmartTabLayout.setSelectedIndicatorColors(Color.BLACK);
        }

        mAppBar.setBackgroundColor(color);
        mToolbar.setOverflowIcon(overflowIcon);

        mSmartTabLayout.setViewPager(mViewPager);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mWatcher.getMap().size() > 0) {
            SubjectTask task = mWatcher.getMap().values().iterator().next();
            bind(task);
        }

        mWatcher.addListener(mValueEventListener);
    }

    @Override
    public void onStop() {
        mWatcher.removeListener(mValueEventListener);
        super.onStop();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    private void bind(@NonNull SubjectTask task) {
        // Set the type of the task as a title
        // of this fragment.
        String title;
        switch (task.type) {
            case SubjectTask.TYPE_QUESTION:
                title = "Question";
                break;
            case SubjectTask.TYPE_ASSIGNMENT:
                title = "Assignment";
                break;
            case SubjectTask.TYPE_ANNOUNCEMENT:
            default:
                title = "Announcement";
                break;
        }
        mToolbar.setTitle(title);
    }

    public static class F extends FragmentBase {


        @Nullable
        @Override
        public View onCreateView(
                LayoutInflater inflater, @Nullable ViewGroup container,
                @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_subject_task_details2, container, false);
        }

    }


}