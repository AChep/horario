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

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.artemchep.horario.Config;
import com.artemchep.horario.R;
import com.artemchep.horario.ui.FormatHelper;
import com.artemchep.horario._new.fragments.FragmentBase;

import java.util.LinkedList;
import java.util.List;

import timber.log.Timber;

/**
 * @author Artem Chepurnoy
 */
public class ChildActivity extends ActivityHorario implements ColorChooserDialog.ColorCallback {

    private static final String TAG_FRAGMENT = "fragment::master";

    public static final String EXTRA_FRAGMENT_NAME = "extra::fragment_name";
    public static final String EXTRA_FRAGMENT_ARGS = "extra::fragment_arguments";

    private List<FormatHelper> mFormatHelpers = new LinkedList<>();

    private FragmentBase mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupTheme();
        setupContent();

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            Bundle args = intent.getBundleExtra(EXTRA_FRAGMENT_ARGS);
            String clazzName = intent.getStringExtra(EXTRA_FRAGMENT_NAME);

            try {
                mFragment = (FragmentBase) Class.forName(clazzName).newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            mFragment.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content, mFragment, TAG_FRAGMENT)
                    .commit();
        } else {
            mFragment = (FragmentBase) getSupportFragmentManager()
                    .getFragment(savedInstanceState, TAG_FRAGMENT);
        }
    }

    protected void setupTheme() {
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
    }

    protected void setupContent() {
        setContentView(R.layout.activity_child);
    }

    @Override
    public void onBackPressed() {
        if (!mFragment.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, @ColorInt int selectedColor) {
        String tag = dialog.tag();
        if (tag == null) {
            Timber.wtf("Selected color, but dialog has no tag?");
            return;
        }

        for (FormatHelper helper : mFormatHelpers) {
            if (tag.startsWith(helper.getTag())) {
                String action = tag.substring(helper.getTag().length());
                helper.onColorSelection(action, selectedColor);
                break;
            }
        }
    }

    public void addFormatHelper(@NonNull FormatHelper formatHelper) {
        mFormatHelpers.add(formatHelper);
    }

    public void removeFormatHelper(@NonNull FormatHelper formatHelper) {
        mFormatHelpers.remove(formatHelper);
    }

}
