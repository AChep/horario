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
package com.artemchep.horario.analytics;

/**
 * @author Artem Chepurnoy
 */
public class AnalyticsParam {

    public static final String NAME = "name";
    public static final String CORRECT = "correct";

    /**
     * @author Artem Chepurnoy
     */
    public static class Navigation {

        public static final String DASHBOARD = "dashboard";
        public static final String NOTIFICATIONS = "notifications";
        public static final String ATTENDANCE = "attendance";
        public static final String LESSONS = "lessons";
        public static final String TEACHERS = "teachers";
        public static final String SUBJECTS = "subjects";
        public static final String NOTES = "notes";
        public static final String EXAMS = "exams";
        /**
         * Support development screen with different monetary
         * and  non-monetary donations.
         */
        public static final String SUPPORT = "donate";
        public static final String SETTINGS = "settings";

    }

    /**
     * @author Artem Chepurnoy
     */
    public static class Support {

        public static final String RATE = "support_rate";
        public static final String SHARE = "support_share";
        public static final String DEV = "support_dev";
        public static final String TRANSLATE = "support_translate";
        public static final String FEEDBACK = "support_feedback";

    }

}
