/*
 * Copyright (C) 2016 Artem Chepurnoy <artemchep@gmail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
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
package com.artemchep.horario.ui.fragments.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.artemchep.horario.R;
import com.artemchep.horario.database.Db;
import com.artemchep.horario.ui.fragments.dialogs.base.DialogFragment;
import com.google.firebase.database.FirebaseDatabase;
import com.shawnlin.numberpicker.NumberPicker;

/**
 * @author Artem Chepurnoy
 */
public class WeekCycleDialog extends DialogFragment {

    public static final String EXTRA_TIMETABLE_PATH = "extra::timetable_path";

    public static final String EXTRA_WEEK_CYCLE = "week_cycle";

    private String mTimetablePath;

    private int mWeekCycle;

    private NumberPicker mNumberPicker;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedState) {
        Activity activity = getActivity();
        assert activity != null;

        Bundle args = getArguments();
        mTimetablePath = args.getString(EXTRA_TIMETABLE_PATH);

        // Load extras
        if (savedState != null) {
            mWeekCycle = savedState.getInt(EXTRA_WEEK_CYCLE);
        } else {
            mWeekCycle = args.getInt(EXTRA_WEEK_CYCLE);
        }

        MaterialDialog md = new MaterialDialog.Builder(activity)
                .customView(R.layout.dialog_edit_week_cycle, true)
                .title(R.string.dialog_week_cycle_title)
                .negativeText(android.R.string.cancel)
                .positiveText(android.R.string.ok)
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(
                            @NonNull MaterialDialog dialog,
                            @NonNull DialogAction which) {
                        switch (which) {
                            case POSITIVE:
                                updateCurrentWeekCycle();

                                FirebaseDatabase.getInstance()
                                        .getReference(mTimetablePath)
                                        .child(Db.LEAF_WEEK_CYCLE)
                                        .setValue(mWeekCycle);
                                break;
                        }
                        dismiss();
                    }
                })
                .autoDismiss(false)
                .build();

        View view = md.getCustomView();
        assert view != null;
        mNumberPicker = (NumberPicker) view.findViewById(R.id.week);
        mNumberPicker.setMinValue(1);
        mNumberPicker.setMaxValue(9);
        mNumberPicker.setValue(mWeekCycle);

        return md;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        updateCurrentWeekCycle();
        outState.putInt(EXTRA_WEEK_CYCLE, mWeekCycle);
    }

    private void updateCurrentWeekCycle() {
        mWeekCycle = mNumberPicker.getValue();
    }

}
