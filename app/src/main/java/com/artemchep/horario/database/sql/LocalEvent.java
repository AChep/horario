package com.artemchep.horario.database.sql;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * @author Artem Chepurnoy
 */
@Entity
public class LocalEvent {

    @NonNull
    @PrimaryKey
    public final String scheduleKey;

    public String teacher;
    public String place;
    public String info;

    public LocalEvent(String scheduleKey, String eventKey) {
        this.scheduleKey = scheduleKey;
    }

}
