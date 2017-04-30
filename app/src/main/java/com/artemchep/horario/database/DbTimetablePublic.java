package com.artemchep.horario.database;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @author Artem Chepurnoy
 */
public class DbTimetablePublic extends Hierarchy {

    private Hierarchy mHierarchy;
    private String mKey;

    DbTimetablePublic(@NonNull Hierarchy h, @Nullable String key) {
        mHierarchy = h;
        mKey = key;
    }

    @Override
    public String path() {
        String path = mKey == null ? "/timetable_public" : "/timetable_public/" + mKey;
        return mHierarchy.path() + path;
    }

}
