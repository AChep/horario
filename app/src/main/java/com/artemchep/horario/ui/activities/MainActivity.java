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
package com.artemchep.horario.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.afollestad.materialcab.MaterialCab;
import com.artemchep.basic.tests.Check;
import com.artemchep.basic.ui.activities.ActivityBase;
import com.artemchep.horario.Config;
import com.artemchep.horario.Heart;
import com.artemchep.horario.R;
import com.artemchep.horario.aggregator.Aggregator;
import com.artemchep.horario.aggregator.Filter;
import com.artemchep.horario.analytics.AnalyticsEvent;
import com.artemchep.horario.analytics.AnalyticsParam;
import com.artemchep.horario.content.PreferenceStore;
import com.artemchep.horario.database.Address;
import com.artemchep.horario.database.Db;
import com.artemchep.horario.database.Persy;
import com.artemchep.horario.interfaces.IProfile;
import com.artemchep.horario.models.Timetable;
import com.artemchep.horario.profiles.Profile;
import com.artemchep.horario.profiles.TimetableProfile;
import com.artemchep.horario.ui.DialogHelper;
import com.artemchep.horario.ui.fragments.dialogs.AboutDialog;
import com.artemchep.horario.ui.fragments.master.AttendanceFragment;
import com.artemchep.horario.ui.fragments.master.DashboardFragment;
import com.artemchep.horario.ui.fragments.master.ExamsFragment;
import com.artemchep.horario.ui.fragments.master.LessonsFragment;
import com.artemchep.horario.ui.fragments.master.ModelFragment;
import com.artemchep.horario.ui.fragments.master.NotesFragment;
import com.artemchep.horario.ui.fragments.master.NotificationsFragment;
import com.artemchep.horario.ui.fragments.master.SubjectsFragment;
import com.artemchep.horario.ui.fragments.master.SupportFragment;
import com.artemchep.horario.ui.fragments.master.TeachersFragment;
import com.artemchep.horario.ui.fragments.master.TimetablesFragment;
import com.artemchep.horario.ui.fragments.preferences.SettingsFragment;
import com.artemchep.horario.ui.widgets.AccountHeaderView;
import com.artemchep.horario.ui.widgets.ContainersLayout;
import com.artemchep.horario.ui.widgets.CustomAppBar;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import timber.log.Timber;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

/**
 * @author Artem Chepurnoy
 */
public class MainActivity extends ActivityBase implements
        FirebaseAuth.AuthStateListener,
        PreferenceStore.OnPreferenceStoreChangeListener,
        NavigationView.OnNavigationItemSelectedListener,
        AccountHeaderView.OnCurrentProfileChangedListener,
        AccountHeaderView.OnProfileClickListener {

    private static final String TAG = "MainActivity";

    private static final String TAG_FRAGMENT_DETAILS = "tag_details";
    private static final String TAG_FRAGMENT_MASTER = "tag_master";

    /**
     * @author Artem Chepurnoy
     */
    public enum State {
        SINGLE_COLUMN_MASTER,
        SINGLE_COLUMN_DETAILS,
        TWO_COLUMNS_EMPTY,
        TWO_COLUMNS_WITH_DETAILS,
    }

    private Config mConfig = Config.getInstance();
    private Persy mPersy = new Persy();

    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mDrawerToggle;
    private AccountHeaderView mAccountHeaderView;
    /*
     * Tablets do have second
     * side navigation view.
     */
    @Nullable
    private NavigationView mNavSideView;
    private NavigationView mNavView;
    private Switch mHolidaysModeSwitch;
    public FloatingActionButton mFab;
    public ContainersLayout mContainers;
    public CustomAppBar mAppBar;
    private MaterialCab mCab;
    private MaterialCab.Callback mCabCallbackExt;

    // Firebase
    private FirebaseAnalytics mAnalytics;
    private FirebaseAuth mAuth;

    /**
     * Current primary fragment,
     * may be {@code null}.
     */
    @Nullable
    private Fragment mMasterFragment;
    /**
     * Current details fragment,
     * may be {@code null}.
     */
    @Nullable
    private Fragment mDetailsFragment;
    @Nullable
    private Address mAddress;
    /**
     * Id of current user,
     * may be {@code null}.
     */
    @Nullable
    private String mUserId;

    private boolean mHolidaysModeBroadcasting;

    @NonNull
    private final MaterialCab.Callback mCabCallback = new MaterialCab.Callback() {

        @Override
        public boolean onCabCreated(MaterialCab cab, Menu menu) {
            // Load theme attrs
            TypedArray a = getTheme().obtainStyledAttributes(
                    new int[]{R.attr.icon_close_grey, android.R.attr.textColorPrimary});
            int iconDrawableRes = a.getResourceId(0, 0);
            int titleTextColor = a.getColor(1, Color.BLACK);
            a.recycle();

            cab.setContentInsetStartRes(R.dimen.mcab_default_content_inset);
            cab.setCloseDrawableRes(iconDrawableRes);
            cab.getToolbar().setTitleTextColor(titleTextColor);
            cab.getToolbar().setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.bg_cab));

            menu.clear();
            return mCabCallbackExt.onCabCreated(cab, menu);
        }

        @Override
        public boolean onCabItemClicked(MenuItem item) {
            return mCabCallbackExt.onCabItemClicked(item);
        }

        @Override
        public boolean onCabFinished(MaterialCab cab) {
            return mCabCallbackExt.onCabFinished(cab);
        }

    };

    @NonNull
    private final Profile mManageProfileItem = new Profile() {
        @DrawableRes
        @Override
        public int getIcon() {
            return R.drawable.ic_settings_grey600_24dp;
        }

        @NonNull
        @Override
        public String getName() {
            return getString(R.string.nav_profile_manage_timetable);
        }
    };

    @NonNull
    private final Profile mAddProfileItem = new Profile() {
        @DrawableRes
        @Override
        public int getIcon() {
            return R.drawable.ic_plus_grey600_24dp;
        }

        @NonNull
        @Override
        public String getName() {
            return getString(R.string.nav_profile_add_timetable);
        }
    };

    /**
     * Timetable event listener that synchronizes profiles
     * with database.
     */
    @NonNull
    private final TimetableEventListener mAccountHeaderUpdateListener = new TimetableEventListener();

    /**
     * @author Artem Chepurnoy
     */
    private class TimetableEventListener implements ChildEventListener {

        private Aggregator<Timetable> mProfileManager = new Aggregator<>(new Filter<Timetable>() {

            @Override
            public boolean isValid(@NonNull Timetable model) {
                return true;
            }

        }, new Comparator<Timetable>() {

            @Override
            public int compare(Timetable o1, Timetable o2) {
                String n1 = o1.name + ""; // in cause #name is null
                String n2 = o2.name + "";
                return n1.compareToIgnoreCase(n2);
            }

        }, new Aggregator.Observer<Timetable>() {

            @Override
            public void add(@NonNull Timetable model, int i) {
                // Insert new profile
                TimetableProfile profile = createProfileItem(model);
                mAccountHeaderView.getProfiles().add(i, profile);
                mAccountHeaderView.notifyProfilesChanged();

                // Select current timetable
                if (mAddress != null && model.key.equals(mAddress.key)) {
                    mAccountHeaderView.setCurrentProfile(profile.getId());
                }
            }

            @Override
            public void set(@NonNull Timetable model, int i) {
                TimetableProfile profile = createProfileItem(model);
                mAccountHeaderView.getProfiles().set(i, profile);
                mAccountHeaderView.notifyProfilesChanged();
            }

            @Override
            public void remove(@NonNull Timetable model, int i) {
                List<IProfile> list = mAccountHeaderView.getProfiles();
                IProfile profile = list.get(i);
                mAccountHeaderView.getProfiles().remove(i);
                mAccountHeaderView.notifyProfilesChanged();

                // Update active profile in cause we just removed it
                // from the list.
                if (profile.getId() == mAccountHeaderView.getCurrentProfile()) {
                    if (list.isEmpty() || !list.get(0).isSelectable()) {
                        mAccountHeaderView.setCurrentProfile(0);
                    } else {
                        TimetableProfile timetableProfile = (TimetableProfile) list.get(0);
                        mAccountHeaderView.setCurrentProfile(timetableProfile.getId());
                    }
                }
            }

            @Override
            public void move(@NonNull Timetable model, int from, int to) {
                throw new RuntimeException("Unsupported operation!");
            }

            @Override
            public void avalanche() {
                throw new RuntimeException("Unsupported operation!");
            }

        });

        /**
         * Creates profile item from timetable.
         */
        @NonNull
        private TimetableProfile createProfileItem(Timetable data) {
            return new TimetableProfile(data);
        }

        @Override
        public void onChildAdded(DataSnapshot snapshot, String s) {
            Timetable model = snapshot.getValue(Timetable.class);
            model.key = snapshot.getKey();
            mProfileManager.put(model);
        }

        @Override
        public void onChildChanged(DataSnapshot snapshot, String s) {
            Timetable model = snapshot.getValue(Timetable.class);
            model.key = snapshot.getKey();
            mProfileManager.put(model);
        }

        @Override
        public void onChildRemoved(DataSnapshot snapshot) {
            Timetable model = snapshot.getValue(Timetable.class);
            model.key = snapshot.getKey();
            mProfileManager.remove(model);
        }

        @Override
        public void onChildMoved(DataSnapshot snapshot, String s) {
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }

        void reset() {
            mProfileManager.reset();
        }

    }


    public MaterialCab getContextualActionBar() {
        return mCab;
    }

    public void startContextualActionBar(@NonNull MaterialCab.Callback callback) {
        finishContextualActionBar();
        mCabCallbackExt = callback;
        mCab.start(mCabCallback);
    }

    public void finishContextualActionBar() {
        if (mCab.isActive()) {
            mCab.finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestCheckout(); // enable in-app-billing for this activity
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

        setContentView(R.layout.activity_main);

        mPersy.start();
        mConfig.addListener(this,
                Config.KEY_HOLIDAY_ON,
                Config.KEY_ADDRESS);
        String address = mConfig.getString(Config.KEY_ADDRESS);
        mAddress = Address.fromString(address);

        // Analytics
        mAnalytics = FirebaseAnalytics.getInstance(this);

        // Setup other views
        mDrawer = (DrawerLayout) findViewById(R.id.drawer);
        mContainers = (ContainersLayout) mDrawer.findViewById(R.id.containers);
        mAppBar = (CustomAppBar) mContainers.findViewById(R.id.activity_main__custom_appbar);
        mFab = (FloatingActionButton) mContainers.findViewById(R.id.fab);

        Toolbar toolbar = mAppBar.hasGeneralToolbar()
                ? mAppBar.getToolbarGeneral()
                : mAppBar.getToolbarSpecific();
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawer, toolbar, 0, 0) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                promptAccountHeader();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                mAccountHeaderView.hideAccountList();
            }

        };
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawer.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        // Setup navigation views
        mNavSideView = (NavigationView) mDrawer.findViewById(R.id.nav_side);
        if (mNavSideView != null) {
            mNavSideView.setNavigationItemSelectedListener(this);
        }
        mNavView = (NavigationView) mDrawer.findViewById(R.id.nav);
        mNavView.setNavigationItemSelectedListener(this);
        mAccountHeaderView = (AccountHeaderView) mNavView.getHeaderView(0).findViewById(R.id.account_header);
        mAccountHeaderView.setOnCurrentProfileChangedListener(this);
        mAccountHeaderView.setOnProfileClickListener(this);
        mAccountHeaderView.setEmptyTitleText(getString(R.string.nav_profile_empty));
        // Setup holidays mode item
        mHolidaysModeSwitch = new Switch(this);
        mHolidaysModeSwitch.setChecked(mConfig.getBoolean(Config.KEY_HOLIDAY_ON));
        mHolidaysModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mHolidaysModeBroadcasting) {
                    return;
                }

                // Commit the changes
                mConfig
                        .edit(MainActivity.this)
                        .put(Config.KEY_HOLIDAY_ON, isChecked)
                        .commit(MainActivity.this);
            }
        });
        mNavView.getMenu().findItem(R.id.nav_holidays_mode).setActionView(mHolidaysModeSwitch);

        if (savedInstanceState != null) {
            mMasterFragment = getSupportFragmentManager().getFragment(savedInstanceState, TAG_FRAGMENT_MASTER);
            mDetailsFragment = getSupportFragmentManager().getFragment(savedInstanceState, TAG_FRAGMENT_DETAILS);

            // Restore CAB only if corresponding fragment still exists
            // and provides the callback, otherwise create it from scratch.
            if (mMasterFragment != null && mMasterFragment instanceof MaterialCab.Callback) {
                mCabCallbackExt = (MaterialCab.Callback) mMasterFragment;
                mCab = MaterialCab.restoreState(savedInstanceState, this, mCabCallback);
            }
        }

        if (mCab == null) {
            mCab = new MaterialCab(this, R.id.cab_stub);
        }

        // Get current user
        mAuth = FirebaseAuth.getInstance();
        mAuth.addAuthStateListener(this);
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            switchToAuthActivity();
            return;
        } else {
            mUserId = user.getUid();
            // register new listener
            DatabaseReference ref = Db.user(mUserId).timetable(null).ref();
            ref.addChildEventListener(mAccountHeaderUpdateListener);

            if (savedInstanceState == null) {
                int id = retrieveStartupItem();
                onNavigationItemSelected(mNavView.getMenu().findItem(id));
            }
        }

        // Show updated changelog each time application version
        // code is increased.
        if (!AboutDialog.isChangelogRead(this)) {
            DialogHelper.showAboutDialog(this, AboutDialog.VIEW_CHANGELOG);
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private int retrieveStartupItem() {
        Intent intent = getIntent();
        // App shortcuts
        if (intent != null && intent.getCategories() != null) {
            Set<String> cat = intent.getCategories();
            if (cat.contains(Heart.CATEGORY_EXAMS)) {
                return R.id.nav_exams;
            } else if (cat.contains(Heart.CATEGORY_NOTES)) {
                return R.id.nav_notes;
            } else if (cat.contains(Heart.CATEGORY_LESSONS)) {
                return R.id.nav_lessons;
            } else if (cat.contains(Heart.CATEGORY_NOTIFICATIONS)) {
                return R.id.nav_notifications;
            }
        }

        int screen = mConfig.getInt(Config.KEY_UI_DEFAULT_SCREEN);
        if (screen == Config.SCREEN_LAST_OPENED) {
            screen = mConfig.getInt(Config.KEY_UI_LAST_SCREEN);
        }

        switch (screen) {
            case Config.SCREEN_DASHBOARD:
                return R.id.nav_overview;
            case Config.SCREEN_NOTIFICATIONS:
                return R.id.nav_notifications;
            case Config.SCREEN_LESSONS:
                return R.id.nav_lessons;
            case Config.SCREEN_SUBJECTS:
                return R.id.nav_subjects;
            case Config.SCREEN_TEACHERS:
                return R.id.nav_teachers;
            case Config.SCREEN_NOTES:
                return R.id.nav_notes;
            case Config.SCREEN_EXAMS:
                return R.id.nav_exams;
            case Config.SCREEN_SUPPORT:
                return R.id.nav_donate;
            case Config.SCREEN_SETTINGS:
                return R.id.nav_settings;
        }

        return R.id.nav_overview;
    }

    /**
     * Launches auth activity and finishes
     * this one.
     */
    private void switchToAuthActivity() {
        Intent intent = new Intent(this, AuthActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        // Do not animate
        overridePendingTransition(0, 0);
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        if (mUserId != null) {
            // Unregister previous listener
            DatabaseReference ref = Db.user(mUserId).timetable(null).ref();
            ref.removeEventListener(mAccountHeaderUpdateListener);
            mAccountHeaderUpdateListener.reset();

            // Clear all profiles
            mAccountHeaderView.getProfiles().clear();
            mAccountHeaderView.getProfiles().add(mAddProfileItem);
            mAccountHeaderView.getProfiles().add(mManageProfileItem);
            mAccountHeaderView.notifyProfilesChanged();
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            mUserId = user.getUid();
            // Register new listener
            DatabaseReference ref = Db.user(mUserId).timetable(null).ref();
            ref.addChildEventListener(mAccountHeaderUpdateListener);
        } else {
            mUserId = null;
            switchToAuthActivity();
        }

        Timber.tag(TAG).d("Auth state changed: current_user=" + mUserId);
    }

    @Override
    public void onBackPressed() {
        if (mAccountHeaderView.isAccountListShown()) {
            // Go back from account view to navigation view
            mAccountHeaderView.hideAccountList();
        } else if (mDrawer.isDrawerOpen(mNavView)) {
            // Go back from navigation view to main frame
            mDrawer.closeDrawer(mNavView);
        } else if (mCab.isActive()) {
            // Finish contextual action bar
            mCab.finish();
        } else if (mDetailsFragment != null) {
            if (mContainers.getState().equals(State.TWO_COLUMNS_WITH_DETAILS)) {
                mAppBar.setState(State.TWO_COLUMNS_EMPTY);
                mContainers.setState(State.TWO_COLUMNS_EMPTY);
            } else if (mContainers.getState().equals(State.SINGLE_COLUMN_DETAILS)) {
                mAppBar.setState(State.SINGLE_COLUMN_MASTER);
                mContainers.setState(State.SINGLE_COLUMN_MASTER);
            } else {
                Timber.tag(TAG).wtf("Details fragment without details-state.");
            }

            getSupportFragmentManager().beginTransaction()
                    .remove(mDetailsFragment)
                    .commit();
            mDetailsFragment = null;
        } else super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        mCab.saveState(outState);

        // Save current fragments
        FragmentManager fm = getSupportFragmentManager();
        if (mMasterFragment != null)
            fm.putFragment(outState, TAG_FRAGMENT_MASTER, mMasterFragment);
        if (mDetailsFragment != null)
            fm.putFragment(outState, TAG_FRAGMENT_DETAILS, mDetailsFragment);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPersy.stop();

        mAuth.removeAuthStateListener(this);
        mConfig.removeListener(this,
                Config.KEY_HOLIDAY_ON,
                Config.KEY_ADDRESS);

        if (mUserId != null) {
            // unregister previous listener
            DatabaseReference ref = Db.user(mUserId).timetable(null).ref();
            ref.removeEventListener(mAccountHeaderUpdateListener);
            mAccountHeaderUpdateListener.reset();
        }
    }

    @Override
    public void onCurrentProfileChanged(@Nullable IProfile profile) {
        if (profile == null) {
            mAddress = null;
        } else {
            TimetableProfile p = (TimetableProfile) profile;
            Timetable t = p.getTimetable();
            mAddress = Address.fromModel(t);
        }

        mConfig.edit(MainActivity.this)
                .put(Config.KEY_ADDRESS, Address.toString(mAddress))
                .commit(this);
    }

    @Override
    public void onProfileClick(@Nullable IProfile profile) {
        if (profile == mAddProfileItem) {
            assert mUserId != null;
            Check.getInstance().isNonNull(mUserId); // otherwise crash
            DialogHelper.showTimetableDialog(this, mUserId, null);
        } else if (profile == mManageProfileItem) {
            // Show timetable manager fragment in
            // a main frame.
            navigateMainFrame(R.id.profile_manage);
            // Clear current checked nav item.
            mNavView.setCheckedItem(R.id.nav_fake);
            if (mNavSideView != null) {
                mNavSideView.setCheckedItem(R.id.nav_fake);
            }
        } else {
            // Switch back to navigation menu on click on
            // actual timetable profile...
            mAccountHeaderView.hideAccountList();
            return;
        }

        // ...otherwise close drawer.
        mDrawer.closeDrawer(mNavView);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        // Sync checked item
        if (mNavSideView != null) {
            mNavSideView.setCheckedItem(id);
        }
        mNavView.setCheckedItem(id);
        // Handle item selection
        if (id == R.id.nav_overview
                || id == R.id.nav_notifications
                || id == R.id.nav_lessons
                || id == R.id.nav_subjects
                || id == R.id.nav_teachers
                || id == R.id.nav_settings
                || id == R.id.nav_attendance
                || id == R.id.nav_notes
                || id == R.id.nav_donate
                || id == R.id.nav_exams) {
            if (mAddress == null) {
                // We can not show fragment without
                // selected timetable.
                return true;
            }

            navigateMainFrame(id);
            mDrawer.closeDrawer(mNavView);
            return true;
        } else if (id == R.id.nav_feedback) {
            DialogHelper.showFeedbackDialog(this);
        } else if (id == R.id.nav_privacy_policy) {
            DialogHelper.showPrivacyDialog(this);
        } else if (id == R.id.nav_about) {
            DialogHelper.showAboutDialog(this);
        } else if (id == R.id.nav_holidays_mode) {
            DialogHelper.showHolidayModeDialog(this);
        } else if (id == R.id.nav_sign_out) {
            mAuth.signOut();
        }

        mDrawer.closeDrawer(mNavView);
        return false;
    }

    private void navigateMainFrame(int id) {
        int screen = Config.SCREEN_NONE;

        Bundle analytics = new Bundle();
        Bundle args = new Bundle();
        Fragment fragment;
        switch (id) {
            case R.id.nav_overview:
                analytics.putString(AnalyticsParam.NAME, AnalyticsParam.Navigation.DASHBOARD);
                fragment = new DashboardFragment();
                screen = Config.SCREEN_DASHBOARD;
                break;
            case R.id.nav_notifications:
                analytics.putString(AnalyticsParam.NAME, AnalyticsParam.Navigation.NOTIFICATIONS);
                fragment = new NotificationsFragment();
                args.putString(ModelFragment.EXTRA_TIMETABLE_PATH, Db
                        .user(mAddress.publicUserKey)
                        .timetablePublic(mAddress.publicKey)
                        .path());
                screen = Config.SCREEN_NOTIFICATIONS;
                break;
            case R.id.nav_attendance:
                analytics.putString(AnalyticsParam.NAME, AnalyticsParam.Navigation.ATTENDANCE);
                fragment = new AttendanceFragment();
                args.putString(ModelFragment.EXTRA_TIMETABLE_PATH, Db
                        .user(mUserId)
                        .timetablePrivate(mAddress.privateKey)
                        .path());
                args.putString(NotesFragment.EXTRA_TIMETABLE_PATH_PUBLIC, Db
                        .user(mAddress.publicUserKey)
                        .timetablePublic(mAddress.publicKey)
                        .path());
                screen = Config.SCREEN_ATTENDANCE;
                break;
            case R.id.nav_lessons:
                analytics.putString(AnalyticsParam.NAME, AnalyticsParam.Navigation.LESSONS);
                fragment = new LessonsFragment();
                args.putString(ModelFragment.EXTRA_TIMETABLE_PATH, Db
                        .user(mAddress.publicUserKey)
                        .timetablePublic(mAddress.publicKey)
                        .path());
                screen = Config.SCREEN_LESSONS;
                break;
            case R.id.nav_subjects:
                analytics.putString(AnalyticsParam.NAME, AnalyticsParam.Navigation.SUBJECTS);
                fragment = new SubjectsFragment();
                args.putString(ModelFragment.EXTRA_TIMETABLE_PATH, Db
                        .user(mAddress.publicUserKey)
                        .timetablePublic(mAddress.publicKey)
                        .path());
                screen = Config.SCREEN_SUBJECTS;
                break;
            case R.id.nav_teachers:
                analytics.putString(AnalyticsParam.NAME, AnalyticsParam.Navigation.TEACHERS);
                fragment = new TeachersFragment();
                args.putString(ModelFragment.EXTRA_TIMETABLE_PATH, Db
                        .user(mAddress.publicUserKey)
                        .timetablePublic(mAddress.publicKey)
                        .path());
                screen = Config.SCREEN_TEACHERS;
                break;
            case R.id.nav_notes:
                analytics.putString(AnalyticsParam.NAME, AnalyticsParam.Navigation.NOTES);
                fragment = new NotesFragment();
                args.putString(ModelFragment.EXTRA_TIMETABLE_PATH, Db
                        .user(mUserId)
                        .timetablePrivate(mAddress.privateKey)
                        .path());
                args.putString(NotesFragment.EXTRA_TIMETABLE_PATH_PUBLIC, Db
                        .user(mAddress.publicUserKey)
                        .timetablePublic(mAddress.publicKey)
                        .path());
                screen = Config.SCREEN_NOTES;
                break;
            case R.id.nav_exams:
                analytics.putString(AnalyticsParam.NAME, AnalyticsParam.Navigation.EXAMS);
                fragment = new ExamsFragment();
                args.putString(ModelFragment.EXTRA_TIMETABLE_PATH, Db
                        .user(mAddress.publicUserKey)
                        .timetablePublic(mAddress.publicKey)
                        .path());
                screen = Config.SCREEN_EXAMS;
                break;
            case R.id.nav_donate:
                analytics.putString(AnalyticsParam.NAME, AnalyticsParam.Navigation.SUPPORT);
                fragment = new SupportFragment();
                screen = Config.SCREEN_SUPPORT;
                break;
            case R.id.nav_settings:
                analytics.putString(AnalyticsParam.NAME, AnalyticsParam.Navigation.SETTINGS);
                fragment = new SettingsFragment();
                screen = Config.SCREEN_SETTINGS;
                break;
            //
            // Timetables
            //
            case R.id.profile_manage:
                analytics.putString(AnalyticsParam.NAME, AnalyticsParam.Navigation.SETTINGS);
                fragment = new TimetablesFragment();
                args.putString(TimetablesFragment.EXTRA_USER_ID, mUserId);
                args.putString(ModelFragment.EXTRA_TIMETABLE_PATH, Db
                        .user(mUserId)
                        .path());
                break;
            default:
                return;
        }

        // Remove cab
        finishContextualActionBar();

        // Save last opened screen
        mConfig.edit(this).put(Config.KEY_UI_LAST_SCREEN, screen).commit();

        // Update layout state
        switch (id) {
            case R.id.nav_subjects:
            case R.id.nav_teachers:
            case R.id.nav_attendance:
            case R.id.nav_exams:
            case R.id.profile_manage:
                mAppBar.setState(State.TWO_COLUMNS_EMPTY);
                mContainers.setState(State.TWO_COLUMNS_EMPTY);
                break;
            default:
                mAppBar.setState(State.SINGLE_COLUMN_MASTER);
                mContainers.setState(State.SINGLE_COLUMN_MASTER);
                break;
        }

        // Disallow editing for timetables-copies
        // (you can not edit parent timetable anyways)
        boolean isCopy = false;
        int cur = mAccountHeaderView.getCurrentProfile();
        for (IProfile profile : mAccountHeaderView.getProfiles()) {
            if (profile.getId() == cur && profile.isSelectable()) {
                TimetableProfile t = (TimetableProfile) profile;
                isCopy = t.getTimetable().isCopy;
                break;
            }
        }
        switch (id) {
            case R.id.nav_overview:
            case R.id.nav_notifications:
            case R.id.nav_lessons:
            case R.id.nav_subjects:
            case R.id.nav_teachers:
            case R.id.nav_exams:
                args.putBoolean(ModelFragment.EXTRA_EDITABLE, !isCopy);
                break;
            case R.id.nav_notes:
                args.putBoolean(ModelFragment.EXTRA_EDITABLE, true);
                break;
            case R.id.profile_manage:
                args.putBoolean(ModelFragment.EXTRA_EDITABLE, true);
                break;
        }

        mAnalytics.logEvent(AnalyticsEvent.SELECT_NAV, analytics);

        fragment.setArguments(args);
        mMasterFragment = fragment;
        mAppBar.setExpanded(true, true);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // Clean-up details fragment if needed
        if (mDetailsFragment != null) {
            transaction.remove(mDetailsFragment);
            mDetailsFragment = null;
        }
        transaction
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.activity_main__frame_master, fragment, TAG_FRAGMENT_MASTER)
                .commit();
    }

    public void navigateDetailsFrame(@NonNull Fragment fragment) {
        mDetailsFragment = fragment;

        if (mAppBar.getState() == State.SINGLE_COLUMN_MASTER) {
            mAppBar.setState(State.SINGLE_COLUMN_DETAILS);
            mContainers.setState(MainActivity.State.SINGLE_COLUMN_DETAILS);
        } else {
            mAppBar.setState(MainActivity.State.TWO_COLUMNS_WITH_DETAILS);
            mContainers.setState(MainActivity.State.TWO_COLUMNS_WITH_DETAILS);
        }

        getSupportFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.activity_main__frame_details, fragment, TAG_FRAGMENT_DETAILS)
                .commit();
    }

    public Persy getPersy() {
        return mPersy;
    }

    @Override
    public void onPreferenceStoreChange(
            @NonNull Context context, @NonNull PreferenceStore.Preference pref,
            @NonNull Object old) {
        switch (pref.key) {
            case Config.KEY_HOLIDAY_ON: {
                mHolidaysModeBroadcasting = true; // prevent loop
                boolean value = (boolean) pref.value;
                mHolidaysModeSwitch.setChecked(value);
                mHolidaysModeBroadcasting = false;
                break;
            }
        }
    }

    // --------------------------
    // -- PROMPTS ---------------
    // --------------------------

    /**
     * If first time, shows a prompt about adding / removing and
     * sharing timetables.
     */
    private void promptAccountHeader() {
        if (mConfig.getBoolean(Config.KEY_PROMPT_ACCOUNT_HEADER)) {
            return;
        }

        new MaterialTapTargetPrompt.Builder(MainActivity.this)
                .setTarget(mAccountHeaderView.findViewById(R.id.menu_icon))
                .setPrimaryText(getString(R.string.prompt_account_header_title))
                .setSecondaryText(getString(R.string.prompt_account_header_summary))
                .setOnHidePromptListener(new MaterialTapTargetPrompt.OnHidePromptListener() {
                    @Override
                    public void onHidePrompt(MotionEvent event, boolean tappedTarget) {
                        mConfig.edit(MainActivity.this)
                                .put(Config.KEY_PROMPT_ACCOUNT_HEADER, true)
                                .commit();
                    }

                    @Override
                    public void onHidePromptComplete() {
                    }
                })
                .setFocalColourAlpha(0)
                .show();

    }

}
