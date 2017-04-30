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
package com.artemchep.horario.profiles;

import android.support.annotation.NonNull;

import com.artemchep.horario.models.Timetable;

/**
 * @author Artem Chepurnoy
 */
public class TimetableProfile extends Profile {

    @NonNull
    private final Timetable mTimetable;

    public TimetableProfile(@NonNull Timetable timetable) {
        mTimetable = timetable;
    }

    @NonNull
    public Timetable getTimetable() {
        return mTimetable;
    }

    /**
     * @return the {@link Timetable#name name} of current timetable
     */
    @NonNull
    @Override
    public String getName() {
        return mTimetable.name;
    }

    /**
     * @return the {@link String#hashCode() hash code}
     * of {@link Timetable#key}.
     */
    @Override
    public int getId() {
        return mTimetable.key.hashCode();
    }

    @Override
    public boolean isSelectable() {
        return true;
    }

}
