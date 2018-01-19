package com.artemchep.horario.database.sql;

import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

//@Database(entities = {LocalEvent.class}, version = 16, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public abstract LocalEventDao eventDao();

    public static AppDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            Context app = context.getApplicationContext();
            INSTANCE = Room.databaseBuilder(app, AppDatabase.class, "events_local")
                    // To simplify the exercise, allow queries on the main thread.
                    // Don't do this on a real app!
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

}