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

import com.artemchep.horario.models.Model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * @author Artem Chepurnoy
 */
public class Subject extends Model {

    public static final Creator<Subject> CREATOR = new Creator<Subject>() {
        @Override
        public Subject[] newArray(int size) {
            return new Subject[size];
        }

        @Override
        public Subject createFromParcel(Parcel source) {
            return new Subject(source);
        }
    };

    public String data;  // key
    public String group;  // key

    public String name;
    public String abbreviation;
    /**
     * Should be one of those
     * {@link com.artemchep.horario.Palette#PALETTE colors}.
     */
    public int color;

    public Subject() {
        // Default constructor required for calls
        // to DataSnapshot.getValue(Subject.class)
    }

    public Subject(Parcel source) {
        key = source.readString();
        data = source.readString();
        group = source.readString();
        name = source.readString();
        abbreviation = source.readString();
        color = source.readInt();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(247, 11)
                .append(key)
                .append(group)
                .append(data)
                .append(name)
                .append(abbreviation)
                .append(color)
                .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof Subject)) return false;

        Subject subject = (Subject) o;
        return new EqualsBuilder()
                .append(color, subject.color)
                .append(key, subject.key)
                .append(data, subject.data)
                .append(group, subject.group)
                .append(name, subject.name)
                .append(abbreviation, subject.abbreviation)
                .isEquals();
    }

    @NonNull
    public Subject clone() {
        return (Subject) super.clone();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(key);
        dest.writeString(data);
        dest.writeString(group);
        dest.writeString(name);
        dest.writeString(abbreviation);
        dest.writeInt(color);
    }

}
