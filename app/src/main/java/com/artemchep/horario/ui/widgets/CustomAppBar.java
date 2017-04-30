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
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.artemchep.horario.R;
import com.artemchep.horario.ui.activities.MainActivity;

/**
 * @author Artem Chepurnoy
 */
public class CustomAppBar extends AppBarLayout {

    private static final String STATE_SUPER = "state_super";
    private static final String STATE_TITLE = "state_title";
    private static final String STATE_TOOLBAR_STATE = "state_toolbar_state";

    @Nullable
    private View mSpaceView;
    @Nullable
    private Toolbar mToolbarGeneral;
    private Toolbar mToolbarSpecific;

    private MainActivity.State mState = MainActivity.State.SINGLE_COLUMN_MASTER;

    public CustomAppBar(Context context) {
        super(context);
        init();
    }

    public CustomAppBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        Context context = getContext();
        LayoutInflater
                .from(context)
                .inflate(R.layout.view_main_toolbar, this, true);

        mToolbarGeneral = (Toolbar) findViewById(R.id.view_main_toolbar__toolbar_general);
        mToolbarSpecific = (Toolbar) findViewById(R.id.view_main_toolbar__toolbar_specific);
        mSpaceView = findViewById(R.id.view_main_toolbar__space_toolbar);
    }

    public void clearMenu() {
        mToolbarSpecific.getMenu().clear();
        mToolbarSpecific.setOnMenuItemClickListener(null);
        if (mToolbarGeneral != null) {
            mToolbarGeneral.getMenu().clear();
            mToolbarGeneral.setOnMenuItemClickListener(null);
        }
    }

    public void clearCustomization() {
        setTitle(null);
        clearMenu();

        // See SupportFragment#setupToolbar()
        if (hasGeneralToolbar()) {
            mToolbarSpecific.setNavigationIcon(null);
        }
    }

    /**
     * Sets the title of {@link #getToolbarSpecific() specific toolbar}.
     */
    public void setTitle(@Nullable String title) {
        mToolbarSpecific.setTitle(title);
    }

    public void setState(@NonNull MainActivity.State state) {
        mState = state;
        switch (state) {
            case SINGLE_COLUMN_MASTER:
            case SINGLE_COLUMN_DETAILS:
                if (mSpaceView != null) {
                    mSpaceView.setVisibility(GONE);
                }
                break;
            case TWO_COLUMNS_EMPTY:
            case TWO_COLUMNS_WITH_DETAILS:
                if (mSpaceView != null) {
                    mSpaceView.setVisibility(VISIBLE);
                }
                break;
        }
    }

    /**
     * @return {@code true} if {@link #getToolbarGeneral() general toolbar}
     * is not {@code null}, {@code false} otherwise.
     * @see #getToolbarGeneral()
     */
    public boolean hasGeneralToolbar() {
        return mToolbarGeneral != null;
    }

    @Nullable
    public Toolbar getToolbarGeneral() {
        return mToolbarGeneral;
    }

    @NonNull
    public Toolbar getToolbarSpecific() {
        return mToolbarSpecific;
    }

    @NonNull
    public MainActivity.State getState() {
        return mState;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(STATE_SUPER, super.onSaveInstanceState());
        bundle.putString(STATE_TITLE, (String) mToolbarSpecific.getTitle());
        bundle.putString(STATE_TOOLBAR_STATE, mState.name());
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable parcelable) {
        if (parcelable instanceof Bundle) {
            Bundle bundle = (Bundle) parcelable;
            mToolbarSpecific.setTitle(bundle.getString(STATE_TITLE));
            setState(MainActivity.State.valueOf(bundle.getString(STATE_TOOLBAR_STATE)));
            parcelable = bundle.getParcelable(STATE_SUPER);
        }
        super.onRestoreInstanceState(parcelable);
    }

}