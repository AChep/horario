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
package com.artemchep.horario.ui.fragments.preferences;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.artemchep.horario.Config;
import com.artemchep.horario.R;
import com.artemchep.horario.ui.activities.MainActivity;
import com.artemchep.horario.ui.widgets.ContainersLayout;
import com.artemchep.horario.ui.widgets.CustomAppBar;

/**
 * @author Artem Chepurnoy
 */
public class SettingsFragment extends PreferenceFragmentBase {

    @NonNull
    private final ListPreferenceSetter mListPreferenceSetter = new ListPreferenceSetter();

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupContainers();
        setupToolbar();
        setupFab();
    }

    protected void setupToolbar() {
        CustomAppBar appBar = getMainActivity().mAppBar;
        appBar.clearCustomization();
        appBar.setTitle(getString(R.string.nav_settings));
    }

    protected void setupFab() {
        getMainActivity().mFab.hide();
    }

    protected void setupContainers() {
        ContainersLayout containers = getMainActivity().mContainers;
        containers.clearCustomization();
    }

    protected MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        syncPreference(Config.KEY_UI_DEFAULT_SCREEN, mListPreferenceSetter);
        syncPreference(Config.KEY_UI_THEME, mListPreferenceSetter);
    }

}
