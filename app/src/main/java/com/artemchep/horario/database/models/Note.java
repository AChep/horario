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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Artem Chepurnoy
 */
public class Note extends Model {

    public static final Creator<Note> CREATOR = new Creator<Note>() {
        @Override
        public Note[] newArray(int size) {
            return new Note[size];
        }

        @Override
        public Note createFromParcel(Parcel source) {
            return new Note(source);
        }
    };

    public String title;
    /**
     * Content text; it is {@link #contentHtml} without
     * html formatting tags.
     */
    public String content;
    public String contentHtml;
    /**
     * First 14 symbols are reserved for UTC time in milliseconds
     * and the other symbols are for actual sorting. String is guaranteed
     * to be at least 14 symbols long.
     */
    public String priority;
    public List<String> subjects;
    /**
     * Estimated date; this kinda transforms our note into
     * a task.
     */
    public int due;

    public Note() {
        // Default constructor required for calls
        // to DataSnapshot.getValue(Note.class)
    }

    public Note(Parcel source) {
        key = source.readString();
        title = source.readString();
        content = source.readString();
        contentHtml = source.readString();
        priority = source.readString();
        source.readStringList(subjects = new ArrayList<>());
        due = source.readInt();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(37, 49)
                .append(key)
                .append(title)
                .append(content)
                .append(contentHtml)
                .append(priority)
                .append(subjects)
                .append(due)
                .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof Note)) return false;

        Note note = (Note) o;
        return new EqualsBuilder()
                .append(due, note.due)
                .append(key, note.key)
                .append(title, note.title)
                .append(content, note.content)
                .append(contentHtml, note.contentHtml)
                .append(priority, note.priority)
                .append(subjects, note.subjects)
                .isEquals();
    }


    @NonNull
    public Note clone() {
        return (Note) super.clone();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(key);
        dest.writeString(title);
        dest.writeString(content);
        dest.writeString(contentHtml);
        dest.writeString(priority);
        dest.writeStringList(subjects);
        dest.writeInt(due);
    }

}