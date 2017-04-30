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

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import com.artemchep.basic.Atomic;
import com.artemchep.horario.models.Model;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * @author Artem Chepurnoy
 */
public class Persy {

    private static final String TAG = "Persy";

    /**
     * The delay before watcher self-destroys after having
     * no active listeners.
     */
    // This is actually the core and the meaning of this class
    private static final long REMOVAL_DELAY_MS = 60 * 1000; // 60 s.

    @NonNull
    private final List<Watcher> mWatchers = new ArrayList<>();

    // This should not leak, because we remove all
    // pending messages by calling #stop()
    @SuppressLint("HandlerLeak")
    @NonNull
    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Watcher watcher = (Watcher) msg.obj;
            Timber.tag(TAG).i("Removing watcher with path=" + watcher.mPath);

            mWatchers.remove(watcher);
        }

    };

    public void start() {
    }

    public void stop() {
        Timber.tag(TAG).i("Clean-up!");
        mHandler.removeCallbacksAndMessages(0);
        mWatchers.clear();
    }

    @CheckResult
    @NonNull
    public <T extends Model> Watcher<T> watchFor(@NonNull Class<T> clazz, @NonNull String path) {
        for (Watcher watcher : mWatchers) {
            if (watcher.mPath.equals(path) && watcher.getType() == clazz) {
                Timber.tag(TAG).i("Re-using watcher with path=" + path);
                //noinspection unchecked
                return watcher;
            }
        }
        // Create new watcher for this
        // database reference.
        Timber.tag(TAG).i("Creating watcher for path=" + path);
        return new Watcher<>(this, clazz, path);
    }

    private void requestAddWatcher(@NonNull Watcher watcher) {
        if (mWatchers.contains(watcher)) {
            mHandler.removeMessages(0, watcher);
        } else {
            Timber.tag(TAG).i("Adding watcher with path=" + watcher.mPath);
            mWatchers.add(watcher);
        }
    }

    private void requestRemoveWatcher(@NonNull Watcher watcher) {
        if (mWatchers.contains(watcher)) {
            Message message = Message.obtain();
            message.what = 0;
            message.obj = watcher;
            mHandler.sendMessageDelayed(message, REMOVAL_DELAY_MS);
        }
    }

    /**
     * @author Artem Chepurnoy
     */
    public static class Watcher<T extends Model> {

        @NonNull
        private final List<ChildEventListener> mListeners = new ArrayList<>();
        @NonNull
        private final Atomic mAtomic = new Atomic(new Atomic.Callback() {
            @Override
            public void onStart(Object... objects) {
                mRef.addChildEventListener(mChildEventListener);
            }

            @Override
            public void onStop(Object... objects) {
                mRef.removeEventListener(mChildEventListener);
            }
        });

        @NonNull
        private final Map<String, T> mMap = new HashMap<>();

        /**
         * Main child event listener.
         */
        @NonNull
        private final ChildEventListener mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                mModel = dataSnapshot.getValue(getType());
                mModel.key = dataSnapshot.getKey();
                mMap.put(mModel.key, mModel);

                // notify everyone
                for (ChildEventListener listener : mListeners) {
                    listener.onChildAdded(dataSnapshot, s);
                }

                mModel = null;
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                mModel = dataSnapshot.getValue(getType());
                mModel.key = dataSnapshot.getKey();
                mMap.put(mModel.key, mModel);

                // notify everyone
                for (ChildEventListener listener : mListeners) {
                    listener.onChildChanged(dataSnapshot, s);
                }

                mModel = null;
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                mMap.remove(dataSnapshot.getKey());

                // notify everyone
                for (ChildEventListener listener : mListeners) {
                    listener.onChildRemoved(dataSnapshot);
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // notify everyone
                for (ChildEventListener listener : mListeners) {
                    listener.onCancelled(databaseError);
                }
            }
        };

        private T mModel;

        @NonNull
        private final String mPath;
        @NonNull
        private final Persy mPersy;
        @NonNull
        private final Class<T> mClazz;
        @NonNull
        private final DatabaseReference mRef;

        public Watcher(@NonNull Persy persy, @NonNull Class<T> clazz, @NonNull String path) {
            mRef = FirebaseDatabase.getInstance().getReference(path);
            mPath = path;
            mClazz = clazz;
            mPersy = persy;
        }

        @NonNull
        public DatabaseReference getDatabase() {
            return mRef;
        }

        public T getModel() {
            return mModel;
        }

        @NonNull
        public String getPath() {
            return mPath;
        }

        @NonNull
        public Class<T> getType() {
            return mClazz;
        }

        @NonNull
        public Map<String, T> getMap() {
            return mMap;
        }

        public void addListener(@NonNull ChildEventListener listener) {
            mPersy.requestAddWatcher(this);

            mListeners.add(listener);
            mAtomic.start();
        }

        public void removeListener(@NonNull ChildEventListener listener) {
            mListeners.remove(listener);
            mAtomic.command(!mListeners.isEmpty());

            mPersy.requestRemoveWatcher(this);
        }

    }

    /**
     * @author Artem Chepurnoy
     */
    public static class ChildEventListenerAdapter implements ChildEventListener {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    }

}