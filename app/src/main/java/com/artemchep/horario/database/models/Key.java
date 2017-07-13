package com.artemchep.horario.database.models;

import android.os.Bundle;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.artemchep.horario.models.Model;
import com.google.firebase.database.Exclude;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by artem on 03.06.2017.
 */

public class Key<T extends Model> extends Model {

    public static final Creator<Key> CREATOR = new Creator<Key>() {
        @Override
        public Key[] newArray(int size) {
            return new Key[size];
        }

        @Override
        public Key createFromParcel(Parcel source) {
            return new Key(source);
        }
    };

    @Exclude
    private T mModel;

    public Key() {
        // Default constructor required for calls
        // to DataSnapshot.getValue(Key.class)
    }

    public Key(Parcel source) {
        key = source.readString();

        Bundle bundle = source.readBundle();
        mModel = bundle.getParcelable("key");
    }

    @Exclude
    public void setModel(@Nullable T model) {
        mModel = model;
    }

    @Exclude
    public T getModel() {
        return mModel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(247, 11)
                .append(key)
                .append(mModel)
                .toHashCode();
    }

    public abstract class Foo {
        abstract Object get();
    }
    public class Bar extends Foo {
        Subject get() {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof Key)) return false;

        Key absence = (Key) o;
        return new EqualsBuilder()
                .append(key, absence.key)
                .append(mModel, absence.mModel)
                .isEquals();
    }

    @NonNull
    public Key clone() {
        return (Key) super.clone();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(key);

        Bundle bundle = new Bundle();
        bundle.putParcelable("key", mModel);
        dest.writeBundle(bundle);
    }

}
