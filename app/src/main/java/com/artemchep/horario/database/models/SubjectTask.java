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
import com.google.firebase.database.Exclude;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * @author Artem Chepurnoy
 */
public class SubjectTask extends Model {

    public static final int TYPE_QUESTION = 1;
    public static final int TYPE_ASSIGNMENT = 2;
    public static final int TYPE_ANNOUNCEMENT = 3;

    public static final Creator<SubjectTask> CREATOR = new Creator<SubjectTask>() {
        @Override
        public SubjectTask[] newArray(int size) {
            return new SubjectTask[size];
        }

        @Override
        public SubjectTask createFromParcel(Parcel source) {
            return new SubjectTask(source);
        }
    };

    public String title;
    public String description;
    public String descriptionHtml;

    /**
     * Should be one of those: {@link #TYPE_QUESTION},
     * {@link #TYPE_ASSIGNMENT}, {@link #TYPE_ANNOUNCEMENT}.
     */
    public int type;

    /**
     * Estimated time;
     * {@code <=0} if not set.
     */
    public int due;

    public String author;

    @Exclude
    public SubjectTaskService more;

    public SubjectTask() {
        // Default constructor required for calls
        // to DataSnapshot.getValue(SubjectTask.class)
    }

    public SubjectTask(Parcel source) {
        key = source.readString();
        title = source.readString();
        description = source.readString();
        descriptionHtml = source.readString();
        type = source.readInt();
        due = source.readInt();

        if (source.readInt() == 1) {
            more = new SubjectTaskService(source);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(247, 11)
                .append(key)
                .append(title)
                .append(description)
                .append(descriptionHtml)
                .append(type)
                .append(due)
                .append(more)
                .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof SubjectTask)) return false;

        SubjectTask subject = (SubjectTask) o;
        return new EqualsBuilder()
                .append(key, subject.key)
                .append(title, subject.title)
                .append(description, subject.description)
                .append(descriptionHtml, subject.descriptionHtml)
                .append(type, subject.type)
                .append(due, subject.due)
                .append(more, subject.more)
                .isEquals();
    }

    @NonNull
    public SubjectTask clone() {
        return (SubjectTask) super.clone();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(key);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(descriptionHtml);
        dest.writeInt(type);
        dest.writeInt(due);

        if (more != null) {
            dest.writeInt(1);
            more.writeToParcel(dest, flags);
        } else dest.writeInt(0);
    }

}
