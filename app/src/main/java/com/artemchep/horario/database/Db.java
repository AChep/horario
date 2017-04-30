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
package com.artemchep.horario.database;

import android.support.annotation.Nullable;

/**
 * @author Artem Chepurnoy
 */
public class Db {

    public static final String LESSONS = "lessons";
    public static final String NOTIFICATIONS = "notifications";
    public static final String SUBJECTS = "subjects";
    public static final String TASKS = "subjects";
    public static final String TEACHERS = "teachers";
    public static final String EXAMS = "exams";
    public static final String NOTES = "notes";
    public static final String ABSENCE = "absence";
    public static final String TIMETABLES = "timetable";
    public static final String TIMETABLES_PUBLIC = "timetable_public";
    public static final String TIMETABLES_PRIVATE = "timetable_private";

    public static final String LEAF_WEEK_CYCLE = "weekCycle";
    public static final String LEAF_IS_SHARED = "isShared";

    public static DbUser user(@Nullable String key) {
        return new DbUser(key);
    }

}
