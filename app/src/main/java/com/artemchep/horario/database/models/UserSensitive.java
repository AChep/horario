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
public class UserSensitive extends Model {

    public static final Creator<UserSensitive> CREATOR = new Creator<UserSensitive>() {
        @Override
        public UserSensitive[] newArray(int size) {
            return new UserSensitive[size];
        }

        @Override
        public UserSensitive createFromParcel(Parcel source) {
            return new UserSensitive(source);
        }
    };

    public String phone;

    public UserSensitive() {
        // Default constructor required for calls
        // to DataSnapshot.getValue(User.class)
    }

    public UserSensitive(Parcel source) {
        key = source.readString();
        phone = source.readString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(247, 11)
                .append(key)
                .append(phone)
                .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof UserSensitive)) return false;

        UserSensitive user = (UserSensitive) o;
        return new EqualsBuilder()
                .append(key, user.key)
                .append(phone, user.phone)
                .isEquals();
    }

    @NonNull
    public UserSensitive clone() {
        return (UserSensitive) super.clone();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(key);
        dest.writeString(phone);
    }

}
