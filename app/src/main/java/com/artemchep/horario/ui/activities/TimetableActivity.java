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

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.artemchep.basic.ui.activities.ActivityBase;
import com.artemchep.horario.Config;
import com.artemchep.horario.R;
import com.artemchep.horario.ui.widgets.SwipeBackLayout;

/**
 * @author Artem Chepurnoy
 */
public class TimetableActivity extends ActivityBase implements
        SwipeBackLayout.SwipeBackListener,
        SwipeBackLayout.FinishListener,
        Toolbar.OnMenuItemClickListener {

    public static final String EXTRA_PATH = "extra::path";
    public static final String EXTRA_NOTE = "note";

    private SwipeBackLayout mSwipeBackLayout;
    private ColorDrawable mColorDrawable;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Apply current theme
        switch (Config.getInstance().getInt(Config.KEY_UI_THEME)) {
            case Config.THEME_LIGHT:
                setTheme(R.style.AppThemeLight_NoActionBar_Translucent);
                break;
            case Config.THEME_DARK:
                setTheme(R.style.AppTheme_NoActionBar_Translucent);
                break;
            case Config.THEME_BLACK:
                setTheme(R.style.AppThemeBlack_NoActionBar_Translucent);
                break;
        }

        setContentView(R.layout.activity_timetable);

        mColorDrawable = new ColorDrawable(Color.BLACK);
        View view = findViewById(R.id.container);
        view.setBackground(mColorDrawable);
        initSwipeBackLayout();
        initToolbar();
    }

    private void initSwipeBackLayout() {
        mSwipeBackLayout = (SwipeBackLayout) findViewById(R.id.swipe_back_layout);
        mSwipeBackLayout.setOnSwipeBackListener(this);
        mSwipeBackLayout.setOnFinishListener(this);
        mSwipeBackLayout.setDragEdge(SwipeBackLayout.DragEdge.TOP);
        mSwipeBackLayout.setSwipeBackEnabled(!isTaskRoot());
    }

    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.inflateMenu(R.menu.master_lessons_week);
        mToolbar.setTitle("Timetable КН-34г");
        mToolbar.setOnMenuItemClickListener(this);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                supportFinishAfterTransition();
            }
        });
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }

    @Override
    public void onViewPositionChanged(float fractionAnchor, float fractionScreen) {
        float alpha = (1 - fractionScreen) * 0.4f;
        mColorDrawable.setAlpha(Math.round(alpha * 255));
    }

    @Override
    public void onFinish() {
        supportFinishAfterTransition();
    }

}
