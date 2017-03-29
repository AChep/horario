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
package com.artemchep.horario.models;

import android.os.Parcel;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * @author Artem Chepurnoy
 */
public class Exam extends Model {

    public static final Creator<Exam> CREATOR = new Creator<Exam>() {
        @Override
        public Exam[] newArray(int size) {
            return new Exam[size];
        }

        @Override
        public Exam createFromParcel(Parcel source) {
            return new Exam(source);
        }
    };

    public String place;
    public String subject;
    public String teacher;
    public String info;
    public int date;
    public int time;

    public Exam() {
        // Default constructor required for calls
        // to DataSnapshot.getValue(Note.class)
    }

    public Exam(Parcel source) {
        key = source.readString();
        place = source.readString();
        subject = source.readString();
        teacher = source.readString();
        info = source.readString();
        date = source.readInt();
        time = source.readInt();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(37, 49)
                .append(key)
                .append(place)
                .append(subject)
                .append(teacher)
                .append(info)
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
        if (!(o instanceof Exam)) return false;

        Exam note = (Exam) o;
        return new EqualsBuilder()
                .append(time, note.time)
                .append(date, note.date)
                .append(key, note.key)
                .append(place, note.place)
                .append(subject, note.subject)
                .append(teacher, note.teacher)
                .append(info, note.info)
                .isEquals();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(key);
        dest.writeString(place);
        dest.writeString(subject);
        dest.writeString(teacher);
        dest.writeString(info);
        dest.writeLong(date);
        dest.writeLong(time);
    }

}
