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
package com.artemchep.horario;

import android.support.annotation.NonNull;

/**
 * Contains a number of build constants mostly redirected from
 * the {@link BuildConfig build config}.
 *
 * @author Artem Chepurnoy
 */
@SuppressWarnings("PointlessBooleanExpression")
public final class Binfo {

    /**
     * Defines if the current build <b>debug</b> or not.
     */
    public static final boolean DEBUG = BuildConfig.MY_DEBUG;
    public static final boolean DEBUG_PROMPTS = DEBUG && false;
    public static final boolean RELEASE = !DEBUG;

    /**
     * The timestamp of build in {@code EEE MMMM dd HH:mm:ss zzz yyyy} format.
     */
    @NonNull
    public static final String TIME_STAMP =
            BuildConfig.MY_TIME;

    @NonNull
    public static final String TIME_STAMP_YEAR =
            BuildConfig.MY_TIME_YEAR;

    /**
     * Public key of my dev account in Google Play.
     */
    @NonNull
    public static final String GOOGLE_PLAY_PUBLIC_KEY =
            BuildConfig.MY_GOOGLE_PLAY_PUBLIC_KEY;

    @NonNull
    public static final String GOOGLE_API_TOKEN =
            "855280611126-ap0li016np9djg6gdsmc3r6mcuc5h1g3.apps.googleusercontent.com";

    /**
     * The official e-mail for tons of complains, billions of
     * "How to uninstall?" screams and one or two useful emails.
     */
    @NonNull
    public static final String SUPPORT_EMAIL =
            "support@artemchep.com";

    public static final boolean IS_TABLET = false;

}
