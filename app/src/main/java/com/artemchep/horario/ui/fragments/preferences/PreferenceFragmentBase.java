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

import android.content.Context;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.TwoStatePreference;
import android.support.annotation.NonNull;

import com.artemchep.horario.Config;
import com.artemchep.horario.content.PreferenceStore;
import com.artemchep.horario.content.PreferenceStoreSyncer;

import timber.log.Timber;

/**
 * @author Artem Chepurnoy
 */
public abstract class PreferenceFragmentBase extends PreferenceFragmentCompat {

    private static final String TAG = "PreferenceFragmentBase";

    private PreferenceStore mConfig;
    private PreferenceStoreSyncer mSyncer;

    private TwoStatePreferenceSetter mTwoStatePreferenceSetter;

    @NonNull
    public PreferenceStore getConfig() {
        return Config.getInstance();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mConfig = getConfig();
        mSyncer = new PreferenceStoreSyncer(mConfig);
    }

    @Override
    public void onResume() {
        super.onResume();
        mSyncer.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        mSyncer.stop();
    }

    /**
     * Synchronizes simple checkbox preference with the config.
     *
     * @param key the key of preference & config's parameter.
     */
    protected void syncPreference(@NonNull String key) {
        if (mTwoStatePreferenceSetter == null)
            mTwoStatePreferenceSetter = new TwoStatePreferenceSetter();
        syncPreference(key, mTwoStatePreferenceSetter);
    }

    /**
     * Synchronizes any preference with the config.
     *
     * @param key    the key of preference & config's parameter.
     * @param setter preference's setter
     * @see TwoStatePreferenceSetter
     */
    protected void syncPreference(@NonNull String key, @NonNull PreferenceStoreSyncer.Setter setter) {
        Preference preference = findPreference(key);

        if (preference == null) {
            Timber.tag(TAG).d("Tried to sync non-existent preference with config.");
            return;
        }

        mSyncer.sync(preference, setter);
    }

    /**
     * The setter for a {@link TwoStatePreference}.
     *
     * @author Artem Chepurnoy
     */
    protected static class TwoStatePreferenceSetter implements PreferenceStoreSyncer.Setter {

        /**
         * {@inheritDoc}
         */
        // This is unneeded, because you should always use
        //     android:summaryOn=""
        //     android:summaryOff=""
        // attributes.
        @Override
        public final void onUpdateSummary(@NonNull Preference preference, @NonNull Object value) {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setValue(@NonNull Preference preference, @NonNull Object value) {
            TwoStatePreference cbp = (TwoStatePreference) preference;
            cbp.setChecked((Boolean) value);
        }

        /**
         * {@inheritDoc}
         */
        @NonNull
        @Override
        public Object getValue(@NonNull Object value) {
            return value;
        }

    }

    /**
     * The setter for a {@link ListPreference}.
     *
     * @author Artem Chepurnoy
     */
    protected static class ListPreferenceSetter implements PreferenceStoreSyncer.Setter {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onUpdateSummary(@NonNull Preference preference, @NonNull Object value) {
            ListPreference cbp = (ListPreference) preference;
            cbp.setSummary(cbp.getEntries()[(Integer) value]);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setValue(@NonNull Preference preference, @NonNull Object value) {
            ListPreference cbp = (ListPreference) preference;
            cbp.setValue(Integer.toString((Integer) value));
        }

        /**
         * {@inheritDoc}
         */
        @NonNull
        @Override
        public Object getValue(@NonNull Object value) {
            return Integer.parseInt((String) value);
        }

    }

}

