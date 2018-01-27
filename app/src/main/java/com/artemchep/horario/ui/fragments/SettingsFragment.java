package com.artemchep.horario.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.artemchep.horario.Binfo;
import com.artemchep.horario.Config;
import com.artemchep.horario.R;
import com.artemchep.horario.interfaces.FragmentCommon;
import com.artemchep.horario.services.SyncSubjectsService;

/**
 * @author Artem Chepurnoy
 */
public class SettingsFragment extends PreferenceFragmentCompat implements FragmentCommon {

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.settings);

        //noinspection StatementWithEmptyBody
        if (Binfo.DEBUG) {
        } else {
            // Remove debug section from settings
            Preference pref = findPreference(Config.KEY_DEBUG);
            pref.getParent().removePreference(pref);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        switch (preference.getKey()) {
            case Config.KEY_DEBUG_SYNC_SCHEDULES:
                final Context context = getContext();
                if (context != null) {
                    Intent intent = new Intent(context, SyncSubjectsService.class);
                    context.startService(intent);
                }
                break;
            default:
                return super.onPreferenceTreeClick(preference);
        }
        return true;
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

}
