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
package com.artemchep.basic.utils.power;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.PowerManager;
import android.support.annotation.NonNull;

import com.artemchep.basic.Atomic;
import com.artemchep.basic.Device;
import com.artemchep.basic.interfaces.IObservable;
import com.artemchep.basic.interfaces.IPowerSave;

import java.util.ArrayList;

/**
 * @author Artem Chepurnoy
 */
public abstract class PowerSaveDetector implements
        IObservable<PowerSaveDetector.OnPowerSaveChanged>,
        IPowerSave {

    private static boolean sPowerSaveMode;

    @NonNull
    public static PowerSaveDetector newInstance(@NonNull Context context) {
        return Device.hasLollipopApi()
                ? new PowerSaveLollipop(context)
                : new PowerSaveCompat(context);
    }

    /**
     * Returns {@code true} if the device is currently in power save mode.
     * When in this mode, applications should reduce their functionality
     * in order to conserve battery as much as possible.
     *
     * @return {@code true} if the device is currently in power save mode, {@code false} otherwise.
     */
    public static boolean isPowerSaving() {
        return sPowerSaveMode;
    }

    protected final ArrayList<OnPowerSaveChanged> mListeners;
    protected final Context mContext;
    protected boolean mPowerSaveMode;

    /**
     * @author Artem Chepurnoy
     */
    public interface OnPowerSaveChanged {

        void onPowerSaveChanged(boolean powerSaving);
    }

    private PowerSaveDetector(@NonNull Context context) {
        mListeners = new ArrayList<>();
        mContext = context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerListener(@NonNull OnPowerSaveChanged listener) {
        mListeners.add(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unregisterListener(@NonNull OnPowerSaveChanged listener) {
        mListeners.remove(listener);
    }

    public abstract void start();

    public abstract void stop();

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPowerSaveMode() {
        return mPowerSaveMode;
    }

    protected void setPowerSaveMode(boolean psm) {
        if (mPowerSaveMode == psm) return;
        sPowerSaveMode = mPowerSaveMode = psm;
        notifyListeners();
    }

    private void notifyListeners() {
        for (OnPowerSaveChanged listener : mListeners) {
            listener.onPowerSaveChanged(mPowerSaveMode);
        }
    }

    /**
     * @author Artem Chepurnoy
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static class PowerSaveLollipop extends PowerSaveDetector {

        private final PowerManager mPowerManager;
        private final BroadcastReceiver mReceiver =
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        switch (intent.getAction()) {
                            case PowerManager.ACTION_POWER_SAVE_MODE_CHANGED:
                                setPowerSaveMode(mPowerManager.isPowerSaveMode());
                                break;
                        }
                    }
                };

        @NonNull
        private final Atomic mAtomic = new Atomic(new Atomic.Callback() {
            @Override
            public void onStart(Object... objects) {
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);
                mContext.registerReceiver(mReceiver, intentFilter);
                setPowerSaveMode(mPowerManager.isPowerSaveMode());
            }

            @Override
            public void onStop(Object... objects) {
                mContext.unregisterReceiver(mReceiver);
            }
        });

        public PowerSaveLollipop(@NonNull Context context) {
            super(context);
            mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        }

        @Override
        public void start() {
            mAtomic.start();
        }

        @Override
        public void stop() {
            mAtomic.stop();
        }
    }

    /**
     * @author Artem Chepurnoy
     */
    private static class PowerSaveCompat extends PowerSaveDetector {

        public PowerSaveCompat(@NonNull Context context) {
            super(context);
        }

        @Override
        public void start() { /* empty */ }

        @Override
        public void stop() { /* empty */ }

    }

}