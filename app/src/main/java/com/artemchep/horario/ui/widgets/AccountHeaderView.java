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
package com.artemchep.horario.ui.widgets;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.internal.NavigationMenuView;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.artemchep.horario.R;
import com.artemchep.horario.interfaces.IProfile;
import com.artemchep.horario.ui.adapters.AccountHeaderAdapter;
import com.artemchep.horario.ui.drawables.MenuIconDrawable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Artem Chepurnoy
 */
public class AccountHeaderView extends RelativeLayout implements View.OnClickListener {

    private static final String BUNDLE_IS_ACCOUNT_SHOWN = "is_account_shown";

    private MenuIconDrawable mMenuIconDrawable = new MenuIconDrawable();
    private NavigationMenuView mNavigationMenuView;
    private TextView mLabelTextView;
    private TextView mTitleTextView;
    private ViewGroup mParent;

    private OnCurrentProfileChangedListener mOnCurrentProfileChangedListener;
    private OnProfileClickListener mOnProfileClickListener;

    private RecyclerView.Adapter mAccountListAdapter;
    private RecyclerView.Adapter mMenuAdapter;

    private boolean mAccountListShown;
    private boolean mInitialized;

    private CharSequence mEmptyTitleText;
    private List<IProfile> mProfiles;
    private int mCurrProfileId;

    /**
     * @author Artem Chepurnoy
     */
    public interface OnCurrentProfileChangedListener {

        void onCurrentProfileChanged(@Nullable IProfile profile);

    }

    /**
     * @author Artem Chepurnoy
     */
    public interface OnProfileClickListener {

        void onProfileClick(@Nullable IProfile profile);

    }

    public AccountHeaderView(Context context) {
        super(context);
        init();
    }

    public AccountHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AccountHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        if (mInitialized) {
            return;
        } else mInitialized = false;

        setOnClickListener(this);
        mProfiles = new ArrayList<>();
        mAccountListAdapter = new AccountHeaderAdapter(mProfiles, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        ImageView menuIcon = (ImageView) findViewById(R.id.menu_icon);
        menuIcon.setImageDrawable(mMenuIconDrawable);

        mTitleTextView = (TextView) findViewById(R.id.title);
        mLabelTextView = (TextView) findViewById(R.id.label);
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0 && mNavigationMenuView == null) {
            mNavigationMenuView = (NavigationMenuView) getParent().getParent();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.account_header:
                toggleAccountList();
                break;
        }
    }

    public void notifyProfileClicked(IProfile profile) {
        if (mOnProfileClickListener != null) {
            mOnProfileClickListener.onProfileClick(profile);
        }

        if (profile.isSelectable()) {
            setCurrentProfile(profile.getId());
        }
    }

    public void notifyProfilesChanged() {
        mAccountListAdapter.notifyDataSetChanged();
        // Update header view
        for (IProfile profile : mProfiles) {
            if (profile.getId() == mCurrProfileId && profile.isSelectable()) {
                mTitleTextView.setText(profile.getName());
                return;
            }
        }
        mTitleTextView.setText(mEmptyTitleText);
    }

    @NonNull
    public List<IProfile> getProfiles() {
        return mProfiles;
    }

    /**
     * @return the id of current item or
     * {@code 0} if not selected
     */
    public int getCurrentProfile() {
        return mCurrProfileId;
    }

    public void setCurrentProfile(int id) {
        if (mCurrProfileId == id) {
            return;
        }

        mCurrProfileId = id;
        notifyProfilesChanged();

        IProfile profile = null;
        for (IProfile p : mProfiles) {
            if (p.getId() == mCurrProfileId && p.isSelectable()) {
                profile = p;
                break;
            }
        }

        if (mOnCurrentProfileChangedListener != null) {
            mOnCurrentProfileChangedListener.onCurrentProfileChanged(profile);
        }
    }

    public void setOnCurrentProfileChangedListener(OnCurrentProfileChangedListener listener) {
        mOnCurrentProfileChangedListener = listener;
    }

    public void setOnProfileClickListener(OnProfileClickListener listener) {
        mOnProfileClickListener = listener;
    }

    public void setEmptyTitleText(@Nullable CharSequence title) {
        mEmptyTitleText = title;
    }

    private void toggleAccountList() {
        if (mAccountListShown) {
            hideAccountList();
        } else showAccountList();
    }

    /**
     * Shows account list and hides navigation menu.
     */
    public void showAccountList() {
        if (mAccountListShown) {
            return;
        } else mAccountListShown = true;

        mMenuIconDrawable.transformToUpsideDown();

        removeViewFromParent();
        mMenuAdapter = mNavigationMenuView.getAdapter();
        mNavigationMenuView.setAdapter(mAccountListAdapter);
    }

    /**
     * Hides account list and shows navigation menu.
     */
    public void hideAccountList() {
        if (mAccountListShown) {
            mAccountListShown = false;
        } else return;

        mMenuIconDrawable.transformToNormal();

        mNavigationMenuView.setAdapter(mMenuAdapter);
        mMenuAdapter = null;
        addViewToParent();
    }

    public boolean isAccountListShown() {
        return mAccountListShown;
    }

    private void removeViewFromParent() {
        getViewParent().removeView(this);
    }

    private void addViewToParent() {
        getViewParent().addView(this);
    }

    private ViewGroup getViewParent() {
        if (mParent == null) {
            mParent = (ViewGroup) getParent();
        }
        return mParent;
    }

}
