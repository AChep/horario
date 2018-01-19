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
package com.artemchep.horario.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.artemchep.horario.Palette;
import com.artemchep.horario.interfaces.FragmentCommon;
import com.artemchep.horario.interfaces.FragmentHost;
import com.artemchep.horario.ui.activities.ChildActivity;
import com.artemchep.horario.ui.utils.ExtrasMapKt;

/**
 * @author Artem Chepurnoy
 */
public abstract class FragmentBase extends Fragment implements FragmentCommon {

    public boolean onBackPressed() {
        return false;
    }

    public void finishToLogin() {
        finish();
    }

    public void finish() {
        FragmentActivity activity = getActivity();
        if (activity instanceof FragmentHost) {
            FragmentHost host = (FragmentHost) activity;
            host.fragmentFinish(this);
        } else if (activity instanceof ChildActivity) {
            activity.supportFinishAfterTransition();
        }
    }

    public void finishToParent() {
        FragmentActivity activity = getActivity();
        if (activity instanceof FragmentHost) {
            FragmentHost host = (FragmentHost) activity;
            host.fragmentFinish(this);
        } else if (activity instanceof ChildActivity) {
            activity.supportFinishAfterTransition();
        }
    }

    public boolean isLarge() {
        return false;
    }

    public int retrieveColor() {
        Bundle args = getArguments();
        assert args != null;

        int defaultColor = Palette.INSTANCE.getUNKNOWN();
        return args.getInt(ExtrasMapKt.EXTRA_COLOR_IMPORTANT,
                args.getInt(ExtrasMapKt.EXTRA_COLOR, defaultColor));
    }

}