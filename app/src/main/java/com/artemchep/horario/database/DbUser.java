package com.artemchep.horario.database;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @author Artem Chepurnoy
 */
public class DbUser extends Hierarchy {

    private String mKey;

    DbUser(@Nullable String key) {
        mKey = key;
    }

    @NonNull
    public DbTimetable timetable(@Nullable String key) {
        return new DbTimetable(this, key);
    }

    @NonNull
    public DbTimetablePublic timetablePublic(@Nullable String key) {
        return new DbTimetablePublic(this, key);
    }

    @NonNull
    public DbTimetablePrivate timetablePrivate(@Nullable String key) {
        return new DbTimetablePrivate(this, key);
    }

    @Override
    public String path() {
        return mKey == null ? "/user" : "/user/" + mKey;
    }

}
