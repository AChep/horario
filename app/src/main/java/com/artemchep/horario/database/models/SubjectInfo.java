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
public class SubjectInfo extends Model {

    public static final Creator<SubjectInfo> CREATOR = new Creator<SubjectInfo>() {
        @Override
        public SubjectInfo[] newArray(int size) {
            return new SubjectInfo[size];
        }

        @Override
        public SubjectInfo createFromParcel(Parcel source) {
            return new SubjectInfo(source);
        }
    };

    public String name;
    public String info;
    public String abbreviation;
    /**
     * Should be one of those
     * {@link com.artemchep.horario.Palette#PALETTE colors}.
     */
    public int color;

    public SubjectInfo() {
        // Default constructor required for calls
        // to DataSnapshot.getValue(SubjectInfo.class)
    }

    public SubjectInfo(Parcel source) {
        key = source.readString();
        name = source.readString();
        info = source.readString();
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
                .append(name)
                .append(info)
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
        if (!(o instanceof SubjectInfo)) return false;

        SubjectInfo subject = (SubjectInfo) o;
        return new EqualsBuilder()
                .append(color, subject.color)
                .append(key, subject.key)
                .append(name, subject.name)
                .append(info, subject.info)
                .append(abbreviation, subject.abbreviation)
                .isEquals();
    }

    @NonNull
    public SubjectInfo clone() {
        return (SubjectInfo) super.clone();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(key);
        dest.writeString(name);
        dest.writeString(info);
        dest.writeString(abbreviation);
        dest.writeInt(color);
    }

}
