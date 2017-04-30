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
package com.artemchep.horario.database;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.artemchep.basic.tests.Check;
import com.artemchep.horario.models.Timetable;
import com.google.firebase.crash.FirebaseCrash;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Artem Chepurnoy
 */
public class Address {

    public static final String EMPTY = "";

    private static final String JSON_KEY = "key";
    private static final String JSON_PRIVATE_KEY = "private_key";
    private static final String JSON_PUBLIC_USER = "public_user";
    private static final String JSON_PUBLIC_KEY = "public_key";

    @NonNull
    public static String toString(@Nullable Address address) {
        if (address == null) {
            return EMPTY;
        }

        JSONObject obj = new JSONObject();
        try {
            obj.put(JSON_KEY, address.key);
            obj.put(JSON_PRIVATE_KEY, address.privateKey);
            obj.put(JSON_PUBLIC_USER, address.publicUserKey);
            obj.put(JSON_PUBLIC_KEY, address.publicKey);
        } catch (JSONException e) {
            FirebaseCrash.report(e);
            return EMPTY;
        }

        return obj.toString();
    }

    @Nullable
    public static Address fromString(@NonNull String string) {
        if (string.equals(EMPTY)) {
            return null;
        }

        Builder builder = new Builder();
        try {
            JSONObject obj = new JSONObject(string);
            builder.setKey(obj.getString(JSON_KEY));
            builder.setPrivate(obj.getString(JSON_PRIVATE_KEY));
            builder.setPublic(
                    obj.getString(JSON_PUBLIC_USER),
                    obj.getString(JSON_PUBLIC_KEY));
        } catch (JSONException e) {
            FirebaseCrash.report(e);
        }

        return builder.build();
    }

    @Nullable
    public static Address fromModel(@Nullable Timetable timetable) {
        if (timetable == null) {
            return null;
        }

        Builder builder = new Builder();
        builder.setKey(timetable.key);
        builder.setPrivate(timetable.privateKey);
        builder.setPublic(timetable.publicAddress);
        return builder.build();
    }

    public String key;
    public String privateKey;
    public String publicUserKey;
    public String publicKey;

    private Address() {
    }

    /**
     * @author Artem Chepurnoy
     */
    public static class Builder {

        private String mKey;
        private String mPrivateKey;
        private String mPublicUserKey;
        private String mPublicKey;

        @NonNull
        public Builder setKey(String key) {
            mKey = key;
            return this;
        }

        @NonNull
        public Builder setPrivate(String key) {
            mPrivateKey = key;
            return this;
        }

        @NonNull
        public Builder setPublic(String userKey, String key) {
            mPublicUserKey = userKey;
            mPublicKey = key;
            return this;
        }

        @NonNull
        public Builder setPublic(String publicAddress) {
            int i = publicAddress.indexOf("/");
            Check.getInstance().isTrue(i >= 0);
            return setPublic(
                    publicAddress.substring(0, i),
                    publicAddress.substring(i + 1));
        }

        @NonNull
        @CheckResult
        public Address build() {
            Address address = new Address();
            address.key = mKey;
            address.privateKey = mPrivateKey;
            address.publicKey = mPublicKey;
            address.publicUserKey = mPublicUserKey;
            return address;
        }

    }

}
