package com.artemchep.horario._new.content;

import android.support.annotation.NonNull;

import com.artemchep.horario.models.Model;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Artem Chepurnoy
 */
public class FirebaseRelational<T extends Model> {

    private final Finder<T> mFinder;
    private final Observer<T> mObserver;

    public void reset() {
        mTmpSet.clear();
        mObserver.clear();
    }

    /**
     * @author Artem Chepurnoy
     */
    public interface Observer<T> {

        void put(@NonNull T model);

        void remove(@NonNull String key);

        void clear();

    }

    private final Set<String> mTmpSet = new HashSet<>();
    private final Set<String> mCurSet = new HashSet<>();

    private ValueEventListener mEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            String key = dataSnapshot.getKey();
            if (dataSnapshot.exists()) {
                Class<T> clazz = mFinder.getType();
                T model = dataSnapshot.getValue(clazz);
                model.key = dataSnapshot.getKey();
                mObserver.put(model);
            } else {
                mObserver.remove(key);
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };

    private boolean mStarted;

    public FirebaseRelational(@NonNull Finder<T> finder, @NonNull Observer<T> observer) {
        mFinder = finder;
        mObserver = observer;
    }

    public void start() {
        if (mStarted) {
            return;
        } else mStarted = true;

        for (String key : mTmpSet) {
            register(key);
            mCurSet.add(key);
        }
        mTmpSet.clear();
    }

    public void stop() {
        if (mStarted) {
            mStarted = false;
        } else return;

        for (String key : mCurSet) {
            unregister(key);
            mTmpSet.add(key);
            mObserver.remove(key);
        }
        mCurSet.clear();
    }

    public void put(@NonNull String key) {
        if (mStarted) {
            register(key);
            mCurSet.add(key);
            return;
        }

        mTmpSet.add(key);
    }

    public void remove(@NonNull String key) {
        if (mStarted) {
            unregister(key);
            mCurSet.remove(key);
            mObserver.remove(key);
        }

        mTmpSet.remove(key);
    }

    private void register(@NonNull String key) {
        String path = mFinder.getPath(key);
        FirebaseDatabase.getInstance().getReference(path).addValueEventListener(mEventListener);
    }

    private void unregister(@NonNull String key) {
        String path = mFinder.getPath(key);
        FirebaseDatabase.getInstance().getReference(path).removeEventListener(mEventListener);
    }

    /**
     * @author Artem Chepurnoy
     */
    public interface Finder<T extends Model> {

        @NonNull
        String getPath(@NonNull String key);

        @NonNull
        Class<T> getType();

    }

}
