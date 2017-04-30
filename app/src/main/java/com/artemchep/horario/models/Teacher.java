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
public class Teacher extends Model {

    public static final Creator<Teacher> CREATOR = new Creator<Teacher>() {
        @Override
        public Teacher[] newArray(int size) {
            return new Teacher[size];
        }

        @Override
        public Teacher createFromParcel(Parcel source) {
            return new Teacher(source);
        }
    };

    public String name;
    public String email;
    public String phone;
    public String info;
    /**
     * Should be one of those
     * {@link com.artemchep.horario.Palette#PALETTE colors}.
     */
    public int color;

    public Teacher() {
        // Default constructor required for calls
        // to DataSnapshot.getValue(Teacher.class)
    }

    public Teacher(Parcel source) {
        key = source.readString();
        name = source.readString();
        email = source.readString();
        phone = source.readString();
        info = source.readString();
        color = source.readInt();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(247, 27)
                .append(key)
                .append(name)
                .append(email)
                .append(phone)
                .append(info)
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
        if (!(o instanceof Teacher)) return false;

        Teacher teacher = (Teacher) o;
        return new EqualsBuilder()
                .append(color, teacher.color)
                .append(key, teacher.key)
                .append(name, teacher.name)
                .append(email, teacher.email)
                .append(phone, teacher.phone)
                .append(info, teacher.info)
                .isEquals();
    }

    @NonNull
    public Teacher clone() {
        return (Teacher) super.clone();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(key);
        dest.writeString(name);
        dest.writeString(email);
        dest.writeString(phone);
        dest.writeString(info);
        dest.writeInt(color);
    }

}
