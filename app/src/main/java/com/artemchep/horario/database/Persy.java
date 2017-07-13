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
import android.support.annotation.Nullable;

import com.artemchep.basic.Atomic;
import com.artemchep.horario.models.Model;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

    private static Persy sPersy;

    public static Persy getInstance() {
        if (sPersy == null) {
            sPersy = new Persy();
        }
        return sPersy;
    }

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

    @NonNull
    private final Atomic mAtomic = new Atomic(new Atomic.Callback() {
        @Override
        public void onStart(Object... objects) {
        }

        @Override
        public void onStop(Object... objects) {
            Timber.tag(TAG).i("Clean-up!");
            mHandler.removeCallbacksAndMessages(0);
            mWatchers.clear();
        }
    }) {

        private int mCount = 0;

        @Override
        public void start(Object... objects) {
            if (mCount++ == 0) {
                super.start(objects);
            }
        }

        @Override
        public void stop(Object... objects) {
            if (--mCount == 0) {
                super.stop(objects);
            }
        }
    };

    public void start() {
        mAtomic.start();
    }

    public void stop() {
        mAtomic.stop();
    }

    @CheckResult
    @NonNull
    public <T extends Model> Watcher<T> watchFor(@NonNull Class<T> clazz, @NonNull String path) {
        Watcher<T> watcher = checkFor(clazz, path);
        if (watcher != null) return watcher;

        // Create new watcher for this
        // database reference.
        Timber.tag(TAG).i("Creating watcher for path=" + path);
        return new Watcher<>(this, clazz, path);
    }

    @CheckResult
    @Nullable
    public <T extends Model> Watcher<T> checkFor(@NonNull Class<T> clazz, @NonNull String path) {
        for (Watcher watcher : mWatchers) {
            if (watcher.mPath.equals(path) && watcher.getType() == clazz) {
                Timber.tag(TAG).i("Re-using watcher with path=" + path);
                //noinspection unchecked
                return watcher;
            }
        }
        return null;
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
    public static class Watcher<T extends Model> implements Atomic.Callback {

        /**
         * Main value event listener.
         */
        @NonNull
        private final ValueEventListener mValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mMap.clear();

                if (dataSnapshot.exists()) {
                    mModel = dataSnapshot.getValue(getType());
                    mModel.key = dataSnapshot.getKey();
                    mMap.put(mModel.key, mModel);
                }

                // notify everyone
                for (ValueEventListener listener : mValueListeners) {
                    listener.onDataChange(dataSnapshot);
                }

                mModel = null;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // notify everyone
                for (ValueEventListener listener : mValueListeners) {
                    listener.onCancelled(databaseError);
                }
            }
        };

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
                for (ChildEventListener listener : mChildListeners) {
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
                for (ChildEventListener listener : mChildListeners) {
                    listener.onChildChanged(dataSnapshot, s);
                }

                mModel = null;
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                mModel = dataSnapshot.getValue(getType());
                mModel.key = dataSnapshot.getKey();
                mMap.remove(mModel.key);

                // notify everyone
                for (ChildEventListener listener : mChildListeners) {
                    listener.onChildRemoved(dataSnapshot);
                }

                mModel = null;
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // notify everyone
                for (ChildEventListener listener : mChildListeners) {
                    listener.onCancelled(databaseError);
                }
            }
        };

        private final Atomic mAtomic = new Atomic(this);
        private final Atomic mAtomicChild = new Atomic(new Atomic.Callback() {
            @Override
            public void onStart(Object... objects) {
                mRef.addChildEventListener(mChildEventListener);
            }

            @Override
            public void onStop(Object... objects) {
                mRef.removeEventListener(mChildEventListener);
            }
        });
        private final Atomic mAtomicValue = new Atomic(new Atomic.Callback() {
            @Override
            public void onStart(Object... objects) {
                mRef.addValueEventListener(mValueEventListener);
            }

            @Override
            public void onStop(Object... objects) {
                mRef.removeEventListener(mValueEventListener);
            }
        });

        @NonNull
        protected final Map<String, T> mMap = new HashMap<>();

        protected T mModel;

        @NonNull
        protected final DatabaseReference mRef;
        @NonNull
        protected final Class<T> mClazz;
        @NonNull
        protected final String mPath;
        @NonNull
        protected final Persy mPersy;

        private final List<ChildEventListener> mChildListeners = new ArrayList<>();
        private final List<ValueEventListener> mValueListeners = new ArrayList<>();

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

        @Nullable
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

        /**
         * Here we should add actual listener from
         * Firebase database.
         *
         * @see #onStop(Object...)
         */
        @Override
        public void onStart(Object... objects) {
            mPersy.requestAddWatcher(this);
        }

        /**
         * Here we should remove actual listener from
         * Firebase database.
         *
         * @see #onStart(Object...)
         */
        @Override
        public void onStop(Object... objects) {
            mPersy.requestRemoveWatcher(this);
        }

        public void addListener(@NonNull ChildEventListener listener) {
            mChildListeners.add(listener);
            mAtomic.start();
            mAtomicChild.start();
        }

        public void addListener(@NonNull ValueEventListener listener) {
            mValueListeners.add(listener);
            mAtomic.start();
            mAtomicValue.start();
        }

        public void removeListener(@NonNull ChildEventListener listener) {
            mChildListeners.remove(listener);

            if (mChildListeners.isEmpty()) {
                mAtomicChild.stop();

                if (mValueListeners.isEmpty()) {
                    mAtomic.stop();
                }
            }
        }

        public void removeListener(@NonNull ValueEventListener listener) {
            mValueListeners.remove(listener);

            if (mValueListeners.isEmpty()) {
                mAtomicValue.stop();

                if (mChildListeners.isEmpty()) {
                    mAtomic.stop();
                }
            }
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
