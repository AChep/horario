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
package com.artemchep.basic;

import android.support.annotation.NonNull;

/**
 * A class for atomic handling start & stop events.
 *
 * @author Artem Chepurnoy
 */
public final class Atomic {

    private static final boolean START = true;
    private static final boolean STOP = false;

    private final Callback mCallback;

    private volatile boolean mRunning;

    /**
     * @author Artem Chepurnoy
     */
    public interface Callback {

        void onStart(Object... objects);

        void onStop(Object... objects);

    }

    public Atomic(@NonNull Callback callback) {
        mCallback = callback;
    }

    public void command(boolean start, Object... objects) {
        if (start == START) {
            start(objects);
        } else stop(objects);
    }

    public void start(Object... objects) {
        synchronized (this) {
            if (mRunning) {
                return;
            }

            mRunning = true;
            mCallback.onStart(objects);
        }
    }

    public void stop(Object... objects) {
        synchronized (this) {
            if (mRunning) {
                mRunning = false;
                mCallback.onStop(objects);
            }
        }
    }

    public boolean isRunning() {
        synchronized (this) {
            return mRunning;
        }
    }

}
