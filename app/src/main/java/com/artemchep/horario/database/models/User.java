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
public class User extends Model {

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User[] newArray(int size) {
            return new User[size];
        }

        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }
    };

    public String name;
    public String info;
    public String email;
    public String avatarUrl;

    public User() {
        // Default constructor required for calls
        // to DataSnapshot.getValue(User.class)
    }

    public User(Parcel source) {
        key = source.readString();
        info = source.readString();
        name = source.readString();
        email = source.readString();
        avatarUrl = source.readString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(247, 11)
                .append(key)
                .append(info)
                .append(name)
                .append(email)
                .append(avatarUrl)
                .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof User)) return false;

        User user = (User) o;
        return new EqualsBuilder()
                .append(key, user.key)
                .append(info, user.info)
                .append(name, user.name)
                .append(email, user.email)
                .append(avatarUrl, user.avatarUrl)
                .isEquals();
    }

    @NonNull
    public User clone() {
        return (User) super.clone();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(key);
        dest.writeString(info);
        dest.writeString(name);
        dest.writeString(email);
        dest.writeString(avatarUrl);
    }

}
