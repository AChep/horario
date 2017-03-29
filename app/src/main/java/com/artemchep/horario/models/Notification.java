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
public class Notification extends Model {

    public static final Creator<Notification> CREATOR = new Creator<Notification>() {
        @Override
        public Notification[] newArray(int size) {
            return new Notification[size];
        }

        @Override
        public Notification createFromParcel(Parcel source) {
            return new Notification(source);
        }
    };

    /**
     * Title of the notification; shortly explains
     * the notification.
     */
    public String title;
    public String summary;
    /**
     * Name of the responsible of this notification
     * person.
     */
    public String author;
    public long timestamp;

    public Notification() {
        // Default constructor required for calls
        // to DataSnapshot.getValue(Notification.class)
    }

    public Notification(Parcel source) {
        key = source.readString();
        title = source.readString();
        summary = source.readString();
        author = source.readString();
        timestamp = source.readLong();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(247, 27)
                .append(key)
                .append(title)
                .append(summary)
                .append(author)
                .append(timestamp)
                .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof Notification)) return false;

        Notification n = (Notification) o;
        return new EqualsBuilder()
                .append(key, n.key)
                .append(title, n.title)
                .append(summary, n.summary)
                .append(author, n.author)
                .append(timestamp, n.timestamp)
                .isEquals();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(key);
        dest.writeString(title);
        dest.writeString(summary);
        dest.writeString(author);
        dest.writeLong(timestamp);
    }

}
