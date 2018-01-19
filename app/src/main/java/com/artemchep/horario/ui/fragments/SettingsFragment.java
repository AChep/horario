package com.artemchep.horario.ui.fragments;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.artemchep.horario.R;
import com.artemchep.horario.interfaces.FragmentCommon;

/**
 * @author Artem Chepurnoy
 */
public class SettingsFragment extends PreferenceFragmentCompat implements FragmentCommon {

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

}
