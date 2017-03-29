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
public class Lesson extends Model {

    public static final Creator<Lesson> CREATOR = new Creator<Lesson>() {
        @Override
        public Lesson[] newArray(int size) {
            return new Lesson[size];
        }

        @Override
        public Lesson createFromParcel(Parcel source) {
            return new Lesson(source);
        }
    };

    public String subject;
    public String teacher;
    public String place;
    public String info;

    /**
     * Time when the lessons starts in minutes, starting
     * from {@code 1}. E.g. {@code 0} means no time set,
     * {@code 2} means 00:01 etc.
     */
    public int timeStart;
    public int timeEnd;
    /**
     * {@code 0} means any week,
     * {@code 1} means first week etc.
     */
    public int week;
    /**
     * {@code 0} means Monday,
     * {@code 2} means Wednesday etc.
     */
    public int day;
    public int type;

    public Lesson() {
        // Default constructor required for calls
        // to DataSnapshot.getValue(Lesson.class)
    }

    public Lesson(Parcel source) {
        key = source.readString();
        subject = source.readString();
        teacher = source.readString();
        place = source.readString();
        info = source.readString();
        timeStart = source.readInt();
        timeEnd = source.readInt();
        day = source.readInt();
        week = source.readInt();
        type = source.readInt();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(79, 271)
                .append(key)
                .append(day)
                .append(week)
                .append(timeStart)
                .append(timeEnd)
                .append(subject)
                .append(teacher)
                .append(place)
                .append(info)
                .append(type)
                .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof Lesson)) return false;

        Lesson lesson = (Lesson) o;
        return new EqualsBuilder()
                .append(type, lesson.type)
                .append(day, lesson.day)
                .append(week, lesson.week)
                .append(timeStart, lesson.timeStart)
                .append(timeEnd, lesson.timeEnd)
                .append(key, lesson.key)
                .append(subject, lesson.subject)
                .append(teacher, lesson.teacher)
                .append(place, lesson.place)
                .append(info, lesson.info)
                .isEquals();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(key);
        dest.writeString(subject);
        dest.writeString(teacher);
        dest.writeString(place);
        dest.writeString(info);
        dest.writeInt(timeStart);
        dest.writeInt(timeEnd);
        dest.writeInt(day);
        dest.writeInt(week);
        dest.writeInt(type);
    }

}
