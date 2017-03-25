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
package com.artemchep.horario;

import android.support.annotation.NonNull;

import com.artemchep.horario.content.PreferenceStore;

import java.util.Map;

/**
 * @author Artem Chepurnoy
 */
public class Config extends PreferenceStore {

    public static final String KEY_CHANGELOG_READ = "prev_changelog_read";

    private static Config sConfig;

    public static synchronized Config getInstance() {
        if (sConfig == null) {
            sConfig = new Config();
        }
        return sConfig;
    }

    private Config() {
        // hidden constructor
    }

    @NonNull
    @Override
    public String getPreferenceName() {
        return "config";
    }

    @Override
    public void loadPreferencesMap(Map<String, Preference> map) {
        map.put(KEY_CHANGELOG_READ, new Preference.Builder()
                .setKey(KEY_CHANGELOG_READ)
                .setClass(int.class)
                .setValue(0) // lowest possible version code
                .build());
    }

}
