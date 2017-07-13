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
public class SubjectTaskService extends Model {

    public static final Creator<SubjectTaskService> CREATOR = new Creator<SubjectTaskService>() {
        @Override
        public SubjectTaskService[] newArray(int size) {
            return new SubjectTaskService[size];
        }

        @Override
        public SubjectTaskService createFromParcel(Parcel source) {
            return new SubjectTaskService(source);
        }
    };

    public long timestamp;
    public long timestampEdited;

    public SubjectTaskService() {
        // Default constructor required for calls
        // to DataSnapshot.getValue(SubjectTaskService.class)
    }

    public SubjectTaskService(Parcel source) {
        key = source.readString();
        timestamp = source.readLong();
        timestampEdited = source.readLong();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(247, 11)
                .append(key)
                .append(timestamp)
                .append(timestampEdited)
                .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof SubjectTaskService)) return false;

        SubjectTaskService subject = (SubjectTaskService) o;
        return new EqualsBuilder()
                .append(key, subject.key)
                .append(timestamp, subject.timestamp)
                .append(timestampEdited, subject.timestampEdited)
                .isEquals();
    }

    @NonNull
    public SubjectTaskService clone() {
        return (SubjectTaskService) super.clone();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(key);
        dest.writeLong(timestamp);
        dest.writeLong(timestampEdited);
    }

}
