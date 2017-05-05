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
package com.artemchep.horario.database.models;

import android.os.Parcel;
import android.support.annotation.NonNull;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * @author Artem Chepurnoy
 */
public class Absence extends Model {

    public static final Creator<Absence> CREATOR = new Creator<Absence>() {
        @Override
        public Absence[] newArray(int size) {
            return new Absence[size];
        }

        @Override
        public Absence createFromParcel(Parcel source) {
            return new Absence(source);
        }
    };

    public String subject; // key

    public String reason;
    public int date;
    public int time;

    public Absence() {
        // Default constructor required for calls
        // to DataSnapshot.getValue(Absence.class)
    }

    public Absence(Parcel source) {
        key = source.readString();
        subject = source.readString();
        reason = source.readString();
        date = source.readInt();
        time = source.readInt();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(247, 11)
                .append(key)
                .append(subject)
                .append(reason)
                .append(date)
                .append(time)
                .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof Absence)) return false;

        Absence absence = (Absence) o;
        return new EqualsBuilder()
                .append(time, absence.time)
                .append(date, absence.date)
                .append(subject, absence.subject)
                .append(reason, absence.reason)
                .append(key, absence.key)
                .isEquals();
    }

    @NonNull
    public Absence clone() {
        return (Absence) super.clone();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(key);
        dest.writeString(subject);
        dest.writeString(reason);
        dest.writeInt(date);
        dest.writeInt(time);
    }

}
