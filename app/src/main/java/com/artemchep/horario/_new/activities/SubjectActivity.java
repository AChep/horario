package com.artemchep.horario._new.activities;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import com.artemchep.basic.Device;
import com.artemchep.basic.tests.Check;
import com.artemchep.horario.Config;
import com.artemchep.horario.R;
import com.artemchep.horario._new.fragments.FragmentSubject;
import com.artemchep.horario._new.fragments.SubjectAboutFragment;
import com.artemchep.horario._new.fragments.SubjectTasksFragment;
import com.artemchep.horario._new.fragments.SubjectMaterialsFragment;
import com.artemchep.horario._new.fragments.SubjectStreamFragment;
import com.artemchep.horario._new.fragments.SubjectStudentsFragment;
import com.artemchep.horario.database.models.SubjectInfo;
import com.artemchep.horario.ui.activities.ActivityHorario;
import com.artemchep.horario.ui.widgets.ContainersLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;
import com.thebluealliance.spectrum.internal.ColorUtil;

import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout;

/**
 * @author Artem Chepurnoy
 */
public class SubjectActivity extends ActivityHorario implements ViewPager.OnPageChangeListener {

    /**
     * @author Artem Chepurnoy
     */
    public interface Page {

        void setSubject(@Nullable SubjectInfo subject);

        void onFabClick(@NonNull View view);

        /**
         * @return {@code true} if this page should have
         * {@link android.support.design.widget.FloatingActionButton fab} shown,
         * {@code false} otherwise
         */
        boolean hasFab();
    }

    public static final String EXTRA_USER_ID = "extra::user::id";
    public static final String EXTRA_TIMETABLE_ID = "extra::timetable::id";
    public static final String EXTRA_SUBJECT = "extra::subject";

    private static final String TAG_FRAGMENT_DETAILS = "tag_details";
    private static final String TAG_FRAGMENT_MASTER = "tag_master";

    private String mUserId;
    private String mTimetableId;
    private SubjectInfo mSubject;

    private AppBarLayout mAppBar;
    private ContainersLayout mContainers;
    private SmartTabLayout mSmartTabLayout;
    private CollapsingToolbarLayout mCollapsingToolbar;
    private FragmentPagerItemAdapter mAdapter;
    private FloatingActionButton mFab;
    private ViewPager mViewPager;
    private View mBackdropToolbar;
    private Toolbar mToolbar;

    @NonNull
    private final ValueEventListener mEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                mSubject = dataSnapshot.getValue(SubjectInfo.class);
                mSubject.key = dataSnapshot.getKey();
                bind(mSubject);
            } //else supportFinishAfterTransition();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };

    @NonNull
    private final View.OnClickListener mFabOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FragmentSubject fragment = (FragmentSubject) mAdapter.getPage(mViewPager.getCurrentItem());
            fragment.onFabClick(v);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Apply current theme
        switch (Config.getInstance().getInt(Config.KEY_UI_THEME)) {
            case Config.THEME_LIGHT:
                setTheme(R.style.AppThemeLight_NoActionBar);
                break;
            case Config.THEME_DARK:
                setTheme(R.style.AppTheme_NoActionBar);
                break;
            case Config.THEME_BLACK:
                setTheme(R.style.AppThemeBlack_NoActionBar);
                break;
        }

        Bundle args = getIntent().getExtras();
        mUserId = args.getString(EXTRA_USER_ID);
        mTimetableId = args.getString(EXTRA_TIMETABLE_ID);
        mSubject = args.getParcelable(EXTRA_SUBJECT);
        // Make sure subject exists and
        // contains at least the key
        Check.getInstance().isNonNull(mSubject);
        Check.getInstance().isNonNull(mSubject.key);

        setContentView(R.layout.activity_subject);

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(mFabOnClickListener);
        mAppBar = (AppBarLayout) findViewById(R.id.appbar);
//        mContainers = (ContainersLayout) findViewById(R.id.containers);
        mSmartTabLayout = (SmartTabLayout) mAppBar.findViewById(R.id.tabs);
        mCollapsingToolbar = (CollapsingToolbarLayout) mAppBar.findViewById(R.id.toolbar_collapsing);
        mBackdropToolbar = mCollapsingToolbar.findViewById(R.id.toolbar_backdrop);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mToolbar = (Toolbar) mCollapsingToolbar.findViewById(R.id.toolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                supportFinishAfterTransition();
            }
        });

        mAdapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(this)
                .add(getString(R.string.__subject_stream), SubjectStreamFragment.class)
                .add(getString(R.string.__subject_tasks), SubjectTasksFragment.class)
                .add(getString(R.string.__subject_materials), SubjectMaterialsFragment.class)
                .add(getString(R.string.__subject_students), SubjectStudentsFragment.class)
                .add(getString(R.string.__subject_about), SubjectAboutFragment.class)
                .create()) {

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                Object item = super.instantiateItem(container, position);
                if (item instanceof Page) {
                    Page f = (Page) item;
                    f.setSubject(mSubject);
                }
                return item;
            }

        };

        mViewPager.setAdapter(mAdapter);
        mViewPager.addOnPageChangeListener(this);

        bind(mSubject);
    }

    @Override
    protected void onStart() {
        super.onStart();
        /*
        FirebaseDatabase.getInstance()
                .getReference(Dbb.SUBJECTS)
                .child(mSubject.key)
                .child(Dbb.SUBJECT_INFO)
                .addValueEventListener(mEventListener);
                */
    }

    @Override
    protected void onStop() {
        /*
        FirebaseDatabase.getInstance()
                .getReference(Dbb.SUBJECTS)
                .child(mSubject.key)
                .child(Dbb.SUBJECT_INFO)
                .removeEventListener(mEventListener);
                */
        super.onStop();
    }

    @SuppressLint("NewApi")
    private void bind(@NonNull SubjectInfo subject) {
        mCollapsingToolbar.setTitle(subject.name);

        int color = subject.color;
        color |= 0xFF000000; // ignore alpha bits
        final boolean isColorDark = ColorUtil.isColorDark(color);

        if (Device.hasLollipopApi()) {
            int statusBarColor = ColorUtils.blendARGB(color, Color.BLACK, 0.4f);
            getWindow().setStatusBarColor(statusBarColor);
        }

        Drawable overflowIcon;
        if (isColorDark) {
            mToolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace_white_24dp);
            mCollapsingToolbar.setExpandedTitleColor(Color.WHITE);
            mCollapsingToolbar.setCollapsedTitleTextColor(Color.WHITE);
            overflowIcon = ContextCompat.getDrawable(this, R.drawable.ic_dots_vertical_white_24dp);
            if (mSmartTabLayout != null) {
                mSmartTabLayout.setDefaultTabTextColor(Color.WHITE);
                mSmartTabLayout.setSelectedIndicatorColors(Color.WHITE);
            }
        } else {
            mToolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace_black_24dp);
            mCollapsingToolbar.setExpandedTitleColor(Color.BLACK);
            mCollapsingToolbar.setCollapsedTitleTextColor(Color.BLACK);
            overflowIcon = ContextCompat.getDrawable(this, R.drawable.ic_dots_vertical_black_24dp);
            if (mSmartTabLayout != null) {
                mSmartTabLayout.setDefaultTabTextColor(Color.BLACK);
                mSmartTabLayout.setSelectedIndicatorColors(Color.BLACK);
            }
        }

        mCollapsingToolbar.setContentScrimColor(color);
        if (mBackdropToolbar != null) {
            mBackdropToolbar.setBackgroundColor(color);
        } else mAppBar.setBackgroundColor(color);
        mToolbar.setOverflowIcon(overflowIcon);

        mSmartTabLayout.setViewPager(mViewPager);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        Page page = (Page) mAdapter.getPage(position);
        if (page.hasFab()) {
            mFab.show();
        } else mFab.hide();
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    public void navigateDetailsFrame(@NonNull Fragment fragment) {
/*
        if (mAppBar.getState() == MainActivity.State.SINGLE_COLUMN_MASTER) {
            mAppBar.setState(MainActivity.State.SINGLE_COLUMN_DETAILS);
            mContainers.setState(MainActivity.State.SINGLE_COLUMN_DETAILS);
        } else {
            mAppBar.setState(MainActivity.State.TWO_COLUMNS_WITH_DETAILS);
            mContainers.setState(MainActivity.State.TWO_COLUMNS_WITH_DETAILS);
        }
        */

        findViewById(R.id.activity_main__frame_details).setVisibility(View.VISIBLE);
        getSupportFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.activity_main__frame_details, fragment, TAG_FRAGMENT_DETAILS)
                .commit();
    }

}