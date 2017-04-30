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
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.artemchep.horario.R;
import com.artemchep.horario.ui.activities.MainActivity;

/**
 * @author Artem Chepurnoy
 */
public class DetailsFragment extends Fragment {

    private Toolbar mToolBar;
    private AppBarLayout mAppBar;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        mAppBar = (AppBarLayout) view.findViewById(R.id.appbar);
//        mToolBar = (Toolbar) mAppBar.findViewById(R.id.toolbar);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupToolbar();
    }

    protected MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }

    protected AppBarLayout getAppBarLayout() {
        return mAppBar;
    }

    protected Toolbar getToolBar() {
        return mToolBar;
    }

    protected void setupToolbar() {
        MainActivity activity = getMainActivity();
        if (!activity.mContainers.hasTwoColumns()) {
//            mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//                }
//            });
        }
    }

}
