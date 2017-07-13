package com.artemchep.horario.database.models;

import android.os.Bundle;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.artemchep.horario.models.Model;
import com.google.firebase.database.Exclude;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Created by artem on 04.06.2017.
 */

public class KeySubjectInfo extends Model {

    public static final Creator<KeySubjectInfo> CREATOR = new Creator<KeySubjectInfo>() {
        @Override
        public KeySubjectInfo[] newArray(int size) {
            return new KeySubjectInfo[size];
        }

        @Override
        public KeySubjectInfo createFromParcel(Parcel source) {
            return new KeySubjectInfo(source);
        }
    };

    @Exclude
    private SubjectInfo mModel;

    public String lol;

    public KeySubjectInfo() {
        // Default constructor required for calls
        // to DataSnapshot.getValue(Key.class)
    }

    public KeySubjectInfo(Parcel source) {
        key = source.readString();

     //   Bundle bundle = source.readBundle();
      //  mModel = bundle.getParcelable("key");
    }

    @Exclude
    public void setModel(@Nullable SubjectInfo model) {
        mModel = model;
    }

    @Exclude
    public SubjectInfo getModel() {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof KeySubjectInfo)) return false;

        KeySubjectInfo absence = (KeySubjectInfo) o;
        return new EqualsBuilder()
                .append(key, absence.key)
                .append(mModel, absence.mModel)
                .isEquals();
    }

    @NonNull
    public KeySubjectInfo clone() {
        return (KeySubjectInfo) super.clone();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(key);
//
     //   Bundle bundle = new Bundle();
     //   bundle.putParcelable("key", mModel);
     //   dest.writeBundle(bundle);
    }

}
