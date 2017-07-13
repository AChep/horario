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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.artemchep.horario._new.activities.SubjectActivity;
import com.artemchep.horario.database.models.Subject;
import com.artemchep.horario.database.models.SubjectInfo;
import com.artemchep.horario.ui.activities.ChildActivity;

/**
 * @author Artem Chepurnoy
 */
public abstract class FragmentSubject extends FragmentBase implements SubjectActivity.Page {

    public abstract void setSubject(@Nullable SubjectInfo subject);

    public void onFabClick(@NonNull View view) {
    }

    /**
     * @return {@code true} if this page should have
     * {@link android.support.design.widget.FloatingActionButton fab} shown,
     * {@code false} otherwise
     */
    public boolean hasFab() {
        return false;
    }

}