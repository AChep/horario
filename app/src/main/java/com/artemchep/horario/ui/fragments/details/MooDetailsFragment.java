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
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.transition.TransitionManager;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.artemchep.horario.Palette;
import com.artemchep.horario.R;
import com.artemchep.horario.models.Model;
import com.artemchep.horario.ui.fragments.master.ModelFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Artem Chepurnoy
 */
// мост в тирабитию
public abstract class MooDetailsFragment<T extends Model> extends DetailsFragment implements
        Toolbar.OnMenuItemClickListener {

    public static final String EXTRA_MODEL = "extra::model";
    public static final String SAVED_MODEL = "saved::model";

    private static final String TAG = "ModelDetailsFragment";

    /**
     * @author Artem Chepurnoy
     */
    protected interface ContentItem<T extends Model> {

        void onSet(@Nullable T model);

        boolean hasChanged(@Nullable T old, @Nullable T model);

    }

    protected abstract static class TextContentItem<T extends Model> implements ContentItem<T> {

        private final View mNoteContainer;
        private final TextView mNoteTextView;

        public TextContentItem(View container, TextView tv) {
            mNoteContainer = container;
            mNoteTextView = tv;
        }

        public abstract String getText(T model);

        @Override
        public void onSet(@Nullable T model) {
            String text = getText(model);
            if (TextUtils.isEmpty(text)) {
                mNoteContainer.setVisibility(View.GONE);
            } else {
                mNoteContainer.setVisibility(View.VISIBLE);
                mNoteTextView.setText(text);
            }
        }

        @Override
        public boolean hasChanged(@Nullable T old, @Nullable T model) {
            return !TextUtils.equals(getText(old), getText(model));
        }
    }

    protected DatabaseReference mDatabaseRef;

    @Nullable
    protected T mModel;
    protected T mModelUi;
    protected String mArgsTimetablePath;
    protected boolean mArgsEditable;

    private Toolbar mToolbar;
    private FloatingActionButton mFab;
    private NestedScrollView mScrollView;
    private MenuItem mFabMenuItem;
    private TextView mStatusView;
    private ViewGroup mContentView;

    private List<ContentItem<T>> mItems = new ArrayList<>();

    @NonNull
    private final DetailsHelper mHelper = new DetailsHelper();

    @NonNull
    private final ValueEventListener mValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                mModel = dataSnapshot.getValue(getType());
                mModel.key = dataSnapshot.getKey();
            } else mModel = null;

            MooDetailsFragment.this.onDataChange();
            updateAll();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };

    public void onDataChange() {

    }

    /**
     * Specific class of this model
     */
    @NonNull
    protected abstract Class<T> getType();

    /**
     * Path to the leaf in database that contains children with
     * current type
     */
    @NonNull
    protected abstract String getPath();

    @Override
    public void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);

        Bundle args = getArguments();
        mArgsTimetablePath = args.getString(ModelFragment.EXTRA_TIMETABLE_PATH);
        mArgsEditable = args.getBoolean(ModelFragment.EXTRA_EDITABLE);

        // Load extras
        if (savedState != null) {
            // Model must not be null here!
            //noinspection ConstantConditions
            mModel = savedState.getParcelable(EXTRA_MODEL);
        } else {
            mModel = args.getParcelable(EXTRA_MODEL);
            assert mModel != null;
            //noinspection unchecked
            mModel = (T) mModel.clone();
        }

        if (mModel != null) {
            mDatabaseRef = FirebaseDatabase.getInstance()
                    .getReference(getPath())
                    .child(mModel.key);
        }
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_details, container, false);

        AppBarLayout appBar = (AppBarLayout) view.findViewById(R.id.appbar);
        appBar.addOnOffsetChangedListener(new AppBarStateChangeListener() {
            @Override
            public void onStateChanged(@NonNull AppBarLayout appBarLayout, @NonNull State state) {
                if (state == State.EXPANDED || state == State.IDLE) {
                    mFabMenuItem.setVisible(false);
                } else {
                    mFabMenuItem.setVisible(true);
                }
            }
        });

        mToolbar = (Toolbar) appBar.findViewById(R.id.toolbar);
        mToolbar.setOnMenuItemClickListener(this);
        mFab = (FloatingActionButton) view.findViewById(R.id.fab);

        mScrollView = (NestedScrollView) view.findViewById(R.id.scroll_view);
        mContentView = onCreateContentView(inflater, mScrollView, mItems, savedInstanceState);
        mScrollView.addView(mContentView);
        mStatusView = (TextView) mScrollView.findViewById(R.id.status); // content view contains it

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mHelper.init(getMainActivity(), view);
        updateAll();
    }

    protected abstract boolean hasAdditionalInfo(@NonNull T model);

    protected abstract ViewGroup onCreateContentView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @NonNull List<ContentItem<T>> contentItems,
            @Nullable Bundle savedInstanceState);

    protected void initWithFab(@IdRes int menuItemId, @DrawableRes int iconRes) {
        mFabMenuItem = mToolbar.getMenu().findItem(menuItemId);
        mFab.setVisibility(View.VISIBLE);
        mFab.setImageResource(iconRes);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMenuItemClick(mFabMenuItem);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mDatabaseRef != null) {
            mDatabaseRef.addValueEventListener(mValueEventListener);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mDatabaseRef != null) {
            mDatabaseRef.removeEventListener(mValueEventListener);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EXTRA_MODEL, mModel);
    }

    public T getModel() {
        return mModel;
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }

    public DetailsHelper getHelper() {
        return mHelper;
    }

    protected void updateAll() {
        /*
        if (ViewCompat.isLaidOut(getView()) && isResumed()) {
            ViewGroup vg = (ViewGroup) getView();
            TransitionManager.beginDelayedTransition(vg);
        }
        */

        if (mModel == null) {
            mHelper.setTitle(null);
            mHelper.setAppBarBackgroundColor(Palette.GREY);

            for (ContentItem<T> item : mItems) { // clear all items
                item.onSet(null);
            }

            mStatusView.setVisibility(View.VISIBLE);
            mStatusView.setText(R.string.details_deleted);

            setMenuItemsEnabled(false);
            mModelUi = (T) mModel.clone();
            return;
        }

        // Update title and app bar layout
        // background color
        updateAppBar(mModel);

        if (hasAdditionalInfo(mModel)) {
            for (ContentItem<T> item : mItems) {
                if (item.hasChanged(mModelUi, mModel)) {
                    item.onSet(mModel);
                }
            }

            mStatusView.setVisibility(View.GONE);
        } else {
            for (ContentItem<T> item : mItems) { // clear all items
                item.onSet(null);
            }

            mStatusView.setVisibility(View.VISIBLE);
            mStatusView.setText(R.string.details_empty);
        }

        setMenuItemsEnabled(true);

        mModelUi = (T) mModel.clone();
    }

    protected void updateAppBar(@NonNull T model) {
    }

    /**
     * Disables all menu items and the
     * {@link #mFab floating action button}.
     */
    private void setMenuItemsEnabled(boolean enabled) {
        Menu menu = mToolbar.getMenu();
        int size = menu.size();
        for (int i = 0; i < size; i++) {
            menu.getItem(i).setEnabled(enabled);
        }

        mFab.setEnabled(enabled);
    }

    /**
     * @author Artem Chepurnoy
     */
    private static abstract class AppBarStateChangeListener implements AppBarLayout.OnOffsetChangedListener {

        enum State {
            EXPANDED,
            COLLAPSED,
            IDLE,
        }

        @NonNull
        private State mCurrentState = State.IDLE;

        @Override
        public final void onOffsetChanged(AppBarLayout appBarLayout, int i) {
            State state;

            if (i == 0) {
                state = State.EXPANDED;
            } else if (Math.abs(i) >= appBarLayout.getTotalScrollRange()) {
                state = State.COLLAPSED;
            } else {
                state = State.IDLE;
            }

            if (mCurrentState != state) {
                mCurrentState = state;
                onStateChanged(appBarLayout, state);
            }
        }

        public abstract void onStateChanged(@NonNull AppBarLayout appBarLayout, @NonNull State state);

    }

}
