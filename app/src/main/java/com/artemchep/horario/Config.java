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
import com.artemchep.horario.database.Address;

import java.util.Map;

/**
 * @author Artem Chepurnoy
 */
public class Config extends PreferenceStore {

    public static final String KEY_ADDRESS = "address";
    public static final String KEY_CHANGELOG_READ = "prev_changelog_read";
    public static final String KEY_HOLIDAY_ON = "holiday_on";
    // Prompt
    public static final String KEY_PROMPT_ACCOUNT_HEADER = "prompt_account_header";
    // Interface
    public static final String KEY_UI_DEFAULT_SCREEN = "ui_default_screen";
    public static final String KEY_UI_LAST_SCREEN = "ui_last_screen";
    public static final int SCREEN_NONE = -1;
    public static final int SCREEN_LAST_OPENED = 0;
    public static final int SCREEN_DASHBOARD = 1;
    public static final int SCREEN_NOTIFICATIONS = 2;
    public static final int SCREEN_ATTENDANCE = 10;
    public static final int SCREEN_LESSONS = 3;
    public static final int SCREEN_SUBJECTS = 4;
    public static final int SCREEN_TEACHERS = 5;
    public static final int SCREEN_NOTES = 6;
    public static final int SCREEN_EXAMS = 7;
    public static final int SCREEN_SUPPORT = 8;
    public static final int SCREEN_SETTINGS = 9;
    public static final String KEY_UI_THEME = "ui_theme";
    public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK = 1;
    public static final int THEME_BLACK = 2;
    public static final String KEY_UI_NOTES_DASHBOARD_VIEW = "ui_notes_dashboard_view";

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
        map.put(KEY_HOLIDAY_ON, new Preference.Builder()
                .setKey(KEY_HOLIDAY_ON)
                .setClass(boolean.class)
                .setValue(false)
                .build());
        map.put(KEY_ADDRESS, new Preference.Builder()
                .setKey(KEY_ADDRESS)
                .setClass(String.class)
                .setValue(Address.EMPTY)
                .build());

        // Prompt
        map.put(KEY_PROMPT_ACCOUNT_HEADER, new Preference.Builder()
                .setKey(KEY_PROMPT_ACCOUNT_HEADER)
                .setClass(boolean.class)
                .setValue(false)
                .build());

        // Interface
        map.put(KEY_UI_DEFAULT_SCREEN, new Preference.Builder()
                .setKey(KEY_UI_DEFAULT_SCREEN)
                .setClass(int.class)
                .setValue(Config.SCREEN_DASHBOARD)
                .build());
        map.put(KEY_UI_LAST_SCREEN, new Preference.Builder()
                .setKey(KEY_UI_LAST_SCREEN)
                .setClass(int.class)
                .setValue(Config.SCREEN_DASHBOARD)
                .build());
        map.put(KEY_UI_THEME, new Preference.Builder()
                .setKey(KEY_UI_THEME)
                .setClass(int.class)
                .setValue(Config.THEME_LIGHT)
                .build());
        map.put(KEY_UI_NOTES_DASHBOARD_VIEW, new Preference.Builder()
                .setKey(KEY_UI_NOTES_DASHBOARD_VIEW)
                .setClass(boolean.class)
                .setValue(true)
                .build());
    }

}
