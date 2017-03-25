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
package com.artemchep.horario.content;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Artem Chepurnoy
 */
public abstract class PreferenceStore {

    private Map<String, Preference> mMap;

    @NonNull
    public abstract String getPreferenceName();

    public abstract void loadPreferencesMap(Map<String, Preference> map);

    public void load(@NonNull Context context) {
        mMap = new HashMap<>();
        loadPreferencesMap(mMap);
        String name = getPreferenceName();
        SharedPreferences sp = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        for (Preference pref : mMap.values()) {
            Object value = pref.value;
            if (boolean.class.isAssignableFrom(pref.clazz)) {
                value = sp.getBoolean(pref.key, (Boolean) value);
            } else if (int.class.isAssignableFrom(pref.clazz)) {
                value = sp.getInt(pref.key, (Integer) value);
            } else if (float.class.isAssignableFrom(pref.clazz)) {
                value = sp.getFloat(pref.key, (Float) value);
            } else if (String.class.isAssignableFrom(pref.clazz)) {
                value = sp.getString(pref.key, (String) value);
            } else if (long.class.isAssignableFrom(pref.clazz)) {
                value = sp.getLong(pref.key, (Long) value);
            } else throw new IllegalArgumentException("Unknown option\'s type.");
            pref.value = value;
        }
    }

    @NonNull
    public Object getObject(@NonNull String key) {
        return mMap.get(key).value;
    }

    @NonNull
    public String getString(@NonNull String key) {
        return (String) getObject(key);
    }

    public int getInt(@NonNull String key) {
        return (Integer) getObject(key);
    }

    public boolean getBoolean(@NonNull String key) {
        return (Boolean) getObject(key);
    }

    @NonNull
    @CheckResult
    public Editor edit(@NonNull Context context) {
        return new Editor(this, context);
    }

    /**
     * @author Artem Chepurnoy
     */
    public static class Editor {

        private final PreferenceStore mStore;
        private final Context mContext;

        private final List<Pair<String, Object>> mList;

        public Editor(@NonNull PreferenceStore ps, @NonNull Context context) {
            mList = new ArrayList<>();
            mContext = context;
            mStore = ps;
        }

        @NonNull
        public Editor put(@NonNull String key, @NonNull Object value) {
            mList.add(new Pair<>(key, value));
            return this;
        }

        public void commit() {
            SharedPreferences.Editor editor = mContext
                    .getSharedPreferences(mStore.getPreferenceName(), Context.MODE_PRIVATE)
                    .edit();
            for (Pair<String, Object> diff : mList) {
                Preference pref = mStore.mMap.get(diff.first);
                pref.value = diff.second;

                // Tell editor to put new value
                if (boolean.class.isAssignableFrom(pref.clazz)) {
                    editor.putBoolean(pref.key, (Boolean) pref.value);
                } else if (int.class.isAssignableFrom(pref.clazz)) {
                    editor.putInt(pref.key, (Integer) pref.value);
                } else if (float.class.isAssignableFrom(pref.clazz)) {
                    editor.putFloat(pref.key, (Float) pref.value);
                } else if (String.class.isAssignableFrom(pref.clazz)) {
                    editor.putString(pref.key, (String) pref.value);
                } else if (long.class.isAssignableFrom(pref.clazz)) {
                    editor.putLong(pref.key, (Long) pref.value);
                } else throw new IllegalArgumentException("Unknown option\'s type.");
            }
            editor.apply();
        }

    }

    /**
     * @author Artem Chepurnoy
     */
    public static class Preference {

        public Class clazz;
        public String key;
        public Object value;

        /**
         * @author Artem Chepurnoy
         */
        public final static class Builder {

            private Class mClazz;
            private String mKey;
            private Object mValue;

            @NonNull
            public Builder setKey(@NonNull String key) {
                mKey = key;
                return this;
            }

            @NonNull
            public Builder setClass(@NonNull Class clazz) {
                mClazz = clazz;
                return this;
            }

            @NonNull
            public Builder setValue(@NonNull Object object) {
                mValue = object;
                return this;
            }

            @NonNull
            @CheckResult
            public Preference build() {
                Preference pref = new Preference();
                pref.key = mKey;
                pref.clazz = mClazz;
                pref.value = mValue;
                return pref;
            }

        }

    }

}
