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
import android.support.annotation.NonNull;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * @author Artem Chepurnoy
 */
public class Timetable extends Model {

    public static final Creator<Timetable> CREATOR = new Creator<Timetable>() {
        @Override
        public Timetable[] newArray(int size) {
            return new Timetable[size];
        }

        @Override
        public Timetable createFromParcel(Parcel source) {
            return new Timetable(source);
        }
    };

    public String name;
    public String privateKey;
    public String publicAddress;
    public boolean isCopy;

    public Timetable() {
        // Default constructor required for calls
        // to DataSnapshot.getValue(Timetable.class)
    }

    public Timetable(Parcel source) {
        key = source.readString();
        name = source.readString();
        privateKey = source.readString();
        publicAddress = source.readString();
        isCopy = source.readInt() != 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(247, 11)
                .append(isCopy)
                .append(key)
                .append(name)
                .append(privateKey)
                .append(publicAddress)
                .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof Timetable)) return false;

        Timetable subject = (Timetable) o;
        return new EqualsBuilder()
                .append(isCopy, subject.isCopy)
                .append(key, subject.key)
                .append(name, subject.name)
                .append(privateKey, subject.privateKey)
                .append(publicAddress, subject.publicAddress)
                .isEquals();
    }

    @NonNull
    public Timetable clone() {
        return (Timetable) super.clone();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(key);
        dest.writeString(name);
        dest.writeString(privateKey);
        dest.writeString(publicAddress);
        dest.writeInt(isCopy ? 1 : 0);
    }

}
