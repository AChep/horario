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
package com.artemchep.horario._new.fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.artemchep.horario.ui.activities.ChildActivity;

/**
 * @author Artem Chepurnoy
 */
public abstract class FragmentBase extends Fragment {

    public boolean onBackPressed() {
        return false;
    }

    public void finishToLogin() {
        finish();
    }

    public void finish() {
        FragmentActivity activity = getActivity();
        if (activity instanceof ChildActivity) {
            activity.supportFinishAfterTransition();
        }
    }

    public void finishToParent() {
        FragmentActivity activity = getActivity();
        if (activity instanceof ChildActivity) {
            activity.supportFinishAfterTransition();
        }
    }

}