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
package com.artemchep.horario.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.afollestad.materialdialogs.MaterialDialog;
import com.artemchep.horario.Config;
import com.artemchep.horario.R;
import com.artemchep.horario.content.PreferenceStore;

/**
 * @author Artem Chepurnoy
 */
public class HolidayModeDialog extends DialogFragment implements
        PreferenceStore.OnPreferenceStoreChangeListener {

    private Switch mSwitch;
    private Config mConfig;

    private boolean mBroadcasting;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mConfig = Config.INSTANCE;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ContextThemeWrapper context = getActivity();
        assert context != null;

        MaterialDialog md = new MaterialDialog.Builder(context)
                .customView(R.layout.dialog_holidays_mode, false)
                .negativeText(R.string.dialog_close)
                .build();
        View view = md.getCustomView();
        assert view != null;
        // Clicking on name toggles the switch
        view.findViewById(R.id.md_titleFrame).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSwitch.toggle();
            }
        });
        // Init switch
        mSwitch = view.findViewById(R.id.switchy);
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mBroadcasting) {
                    return;
                }

                int theme =Config.INSTANCE.get(Config.KEY_UI_THEME);
                Config.INSTANCE.edit(getContext()).put(Config.KEY_UI_THEME,theme== Config.THEME_LIGHT?Config.THEME_DARK:Config.THEME_LIGHT).commit();
//                Config.getInstance()
//                        .edit(getContext())
//                        .put(Config.KEY_HOLIDAY_ON, isChecked)
//                        .commit(HolidayModeDialog.this);
            }
        });
        return md;
    }

    @Override
    public void onStart() {
        super.onStart();

//        mConfig.addListener(this, Config.KEY_HOLIDAY_ON);
//        mSwitch.setChecked(mConfig.getBoolean(Config.KEY_HOLIDAY_ON));
    }

    @Override
    public void onStop() {
        super.onStop();

//        mConfig.removeListener(this, Config.KEY_HOLIDAY_ON);
    }

    @Override
    public void onPreferenceStoreChange(
            @NonNull Context context, @NonNull PreferenceStore.Preference pref,
            @NonNull Object old) {
//        if (Binfo.DEBUG) Check.getInstance().isTrue(pref.key.equals(Config.KEY_HOLIDAY_ON));
        mBroadcasting = true;

        boolean value = (boolean) pref.value;
        mSwitch.setChecked(value);

        mBroadcasting = false;
    }

}