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

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.artemchep.horario.R;
import com.artemchep.horario.ui.activities.MainActivity;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.thebluealliance.spectrum.internal.ColorUtil;

import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout;

/**
 * @author Artem Chepurnoy
 */
class DetailsHelper {

    private MainActivity mActivity;

    private AppBarLayout mAppBar;
    private CollapsingToolbarLayout mCollapsingToolbar;
    private SmartTabLayout mSmartTabLayout;
    private View mBackdropToolbar;
    private Toolbar mToolbar;

    void init(@NonNull MainActivity activity, @NonNull View view) {
        mActivity = activity;

        mAppBar = (AppBarLayout) view.findViewById(R.id.appbar);
        mSmartTabLayout = (SmartTabLayout) mAppBar.findViewById(R.id.viewpagertab);
        mCollapsingToolbar = (CollapsingToolbarLayout) mAppBar.findViewById(R.id.toolbar_collapsing);
        mBackdropToolbar = mCollapsingToolbar.findViewById(R.id.toolbar_backdrop);
        mToolbar = (Toolbar) mCollapsingToolbar.findViewById(R.id.toolbar);
    }

    void setTitle(@Nullable String title) {
        mCollapsingToolbar.setTitle(title);
    }

    void setAppBarBackgroundColor(@ColorInt int color) {
        color |= 0xFF000000; // ignore alpha bits
        final boolean isColorDark = ColorUtil.isColorDark(color);

        Drawable overflowIcon;
        if (isColorDark) {
            mCollapsingToolbar.setExpandedTitleColor(Color.WHITE);
            mCollapsingToolbar.setCollapsedTitleTextColor(Color.WHITE);
            overflowIcon = ContextCompat.getDrawable(mActivity, R.drawable.ic_dots_vertical_white_24dp);
            if (mSmartTabLayout != null) {
                mSmartTabLayout.setDefaultTabTextColor(Color.WHITE);
                mSmartTabLayout.setSelectedIndicatorColors(Color.WHITE);
            }
        } else {
            mCollapsingToolbar.setExpandedTitleColor(Color.BLACK);
            mCollapsingToolbar.setCollapsedTitleTextColor(Color.BLACK);
            overflowIcon = ContextCompat.getDrawable(mActivity, R.drawable.ic_dots_vertical_black_24dp);
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

        if (mActivity.mContainers.hasSingleColumn()) {
            int iconRes;
            if (mActivity.mAppBar.hasGeneralToolbar()) {
                iconRes = isColorDark
                        ? R.drawable.ic_close_white_24dp
                        : R.drawable.ic_close_black_24dp;
            } else iconRes = isColorDark
                    ? R.drawable.ic_backburger_white_24dp
                    : R.drawable.ic_backburger_black_24dp;
            Drawable navIcon = ContextCompat.getDrawable(mActivity, iconRes);
            mToolbar.setNavigationIcon(navIcon);
        }

        MenuItem editItem = mToolbar.getMenu().findItem(R.id.action_edit);
        if (editItem != null) {
            Drawable editIcon = isColorDark
                    ? ContextCompat.getDrawable(mActivity, R.drawable.ic_pencil_white_24dp)
                    : ContextCompat.getDrawable(mActivity, R.drawable.ic_pencil_black_24dp);
            editItem.setIcon(editIcon);
        }

        MenuItem shareItem = mToolbar.getMenu().findItem(R.id.action_share);
        if (shareItem != null) {
            Drawable shareIcon = isColorDark
                    ? ContextCompat.getDrawable(mActivity, R.drawable.ic_share_variant_white_24dp)
                    : ContextCompat.getDrawable(mActivity, R.drawable.ic_share_variant_black_24dp);
            shareItem.setIcon(shareIcon);
        }
    }

}
