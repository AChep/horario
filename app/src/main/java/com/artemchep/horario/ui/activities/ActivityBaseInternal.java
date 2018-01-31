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
package com.artemchep.horario.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.artemchep.horario.tests.Check;
import com.artemchep.horario.utils.power.PowerSaveDetector;
import com.artemchep.horario.Heart;
import com.artemchep.horario.interfaces.IActivityBase;

import org.solovyev.android.checkout.ActivityCheckout;
import org.solovyev.android.checkout.Checkout;

/**
 * @author Artem Chepurnoy
 */
final class ActivityBaseInternal implements IActivityBase {

    private static final String TAG = "ActivityBaseInternal";

    private Activity mActivity;
    private ActivityCheckout mCheckout;
    private PowerSaveDetector mPowerSaveDetector;

    private boolean mCheckoutRequest;
    private boolean mCreated;

    /* Mirrors Activity#onCreate(...) */
    void onCreate(Activity activity, Bundle savedInstanceState) {
        if (mCheckoutRequest) {
            mCheckout = Checkout.forActivity(activity, Heart.getBilling(activity));
            mCheckout.start();
        }

        mPowerSaveDetector = PowerSaveDetector.newInstance(activity);
        mActivity = activity;
        mCreated = true;
    }

    /* Mirrors Activity#onStart(...) */
    void onStart() {
        mPowerSaveDetector.start();
    }

    /* Mirrors Activity#onStop(...) */
    void onStop() {
        mPowerSaveDetector.stop();
    }

    /* Mirrors Activity#onDestroy(...) */
    void onDestroy() {
        if (mCheckout != null) {
            mCheckout.stop();
            mCheckout = null;
        }
    }

    /* Mirrors Activity#onActivityResult(...) */
    boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        return mCheckout != null && mCheckout.onActivityResult(requestCode, resultCode, data);
    }

    //-- IActivityBase --------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public void requestCheckout() {
        Check.getInstance().isFalse(mCheckoutRequest); // do not request twice
        Check.getInstance().isFalse(mCreated); // not created yet.
        mCheckoutRequest = true;
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public Activity getActivity() {
        return mActivity;
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public ActivityCheckout getCheckout() {
        return mCheckout;
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public PowerSaveDetector getPowerSaveDetector() {
        return mPowerSaveDetector;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPowerSaveMode() {
        return mPowerSaveDetector.isPowerSaveMode();
    }

}
