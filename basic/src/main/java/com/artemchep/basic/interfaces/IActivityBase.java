/*
 * Copyright (C) 2017 Artem Chepurnoy <artemchep@gmail.com>
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
package com.artemchep.basic.interfaces;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.artemchep.basic.utils.power.PowerSaveDetector;

import org.solovyev.android.checkout.ActivityCheckout;

/**
 * @author Artem Chepurnoy
 */
public interface IActivityBase extends IPowerSave {

    /**
     * Requests to load the {@link org.solovyev.android.checkout.Checkout} on activity
     * create. <b>Must be called from {@link Activity#onCreate(...)}!</b>
     */
    void requestCheckout();

    @NonNull
    Activity getActivity();

    @Nullable
    ActivityCheckout getCheckout();

    /**
     * @return active power save detector to determine if power-save mode is
     * turned on or turned off.
     * @see #isPowerSaveMode()
     */
    @NonNull
    PowerSaveDetector getPowerSaveDetector();

}
