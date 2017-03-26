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
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.artemchep.basic.Atomic;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

/**
 * @author Artem Chepurnoy
 */
public class PreferenceStoreSyncer implements Atomic.Callback {

    private static final String TAG = "PreferenceStoreSyncer";

    private final Atomic mAtomic = new Atomic(this);
    private final Map<String, Pair<Preference, Setter>> mMap;
    private final PreferenceStore mStore;

    @NonNull
    private final PreferenceStore.OnPreferenceStoreChangeListener mStoreListener =
            new PreferenceStore.OnPreferenceStoreChangeListener() {
                @Override
                public void onPreferenceStoreChange(
                        @NonNull Context context,
                        @NonNull PreferenceStore.Preference pref,
                        @NonNull Object old) {
                    updatePreference(pref.key);
                }
            };

    @NonNull
    private final Preference.OnPreferenceChangeListener mPrefListener =
            new Preference.OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (mBroadcasting) {
                        return true;
                    }

                    for (Map.Entry<String, Pair<Preference, Setter>> entry : mMap.entrySet()) {
                        if (entry.getValue().first == preference) {
                            Setter setter = entry.getValue().second;
                            mStore
                                    .edit(preference.getContext())
                                    .put(entry.getKey(), setter.getValue(newValue))
                                    .commit(mStoreListener);
                            setter.onUpdateSummary(entry.getValue().first, newValue);
                            return true;
                        }
                    }

                    Timber.tag(TAG).w("Changed preference is not found in map!");
                    return false;
                }
            };

    private boolean mBroadcasting;

    public PreferenceStoreSyncer(@NonNull PreferenceStore store) {
        mMap = new HashMap<>();
        mStore = store;
    }

    public void sync(@NonNull Preference preference, @NonNull Setter setter) {
        sync(preference, setter, preference.getKey());
    }

    public void sync(@NonNull Preference preference, @NonNull Setter setter, @NonNull String key) {
        mMap.put(key, new Pair<>(preference, setter));

        if (mAtomic.isRunning()) {
            mStore.addListener(mStoreListener, key);
            preference.setOnPreferenceChangeListener(mPrefListener);
            updatePreference(key);
        }
    }

    /**
     * Updates all preferences and starts to listen to the changes.
     * Don't forget to call {@link #stop()} later!
     *
     * @see #stop()
     */
    public void start() {
        mAtomic.start();
    }

    public void stop() {
        mAtomic.stop();
    }

    @Override
    public void onStart(Object... objects) {
        String[] keys = keysArray();
        mStore.addListener(mStoreListener, keys);
        for (String key : keys) updatePreference(key);
    }

    @Override
    public void onStop(Object... objects) {
        mStore.removeListener(mStoreListener, keysArray());
    }

    private void updatePreference(@NonNull String key) {
        mBroadcasting = true;

        Object value = mStore.getObject(key);
        Pair<Preference, Setter> pair = mMap.get(key);
        pair.second.setValue(pair.first, value);
        pair.second.onUpdateSummary(pair.first, value);

        mBroadcasting = false;
    }

    @NonNull
    private String[] keysArray() {
        Set<String> set = mMap.keySet();
        return set.toArray(new String[set.size()]);
    }

    /**
     * @author Artem Chepurnoy
     */
    public interface Setter {

        void onUpdateSummary(@NonNull Preference preference, @NonNull Object value);

        /**
         * Sets new value to the preference.
         *
         * @param preference preference to set to
         * @param value      new value to set
         */
        void setValue(@NonNull Preference preference, @NonNull Object value);

        @NonNull
        Object getValue(@NonNull Object value);

    }

}
