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
package com.artemchep.horario;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Base64;

/**
 * @author Artem Chepurnoy
 */
public class ShareHelper {

    private String mUserId;
    private String mPublicKey;

    public void decode(@NonNull String userId, @NonNull String publicKey) {
        mUserId = userId;
        mPublicKey = publicKey;
    }

    public void decode(@NonNull String key) {
        decrypting:
        {
            try {
                key = new String(Base64.decode(key, Base64.URL_SAFE));
            } catch (IllegalArgumentException e) {
                break decrypting;
            }
            String[] parts = key.split("/");

            // It contains hash of the address and address
            // itself: user_id and timetable_public_key.
            if (parts.length != 3) {
                break decrypting;
            }

            int hash;
            try {
                hash = Integer.parseInt(parts[0]);
            } catch (NumberFormatException e) {
                break decrypting;
            }

            // Check if hashes do match: otherwise user
            // manipulated with share-link.
            String address = parts[1] + "/" + parts[2];
            if (address.hashCode() != hash) {
                break decrypting;
            }

            mUserId = parts[1];
            mPublicKey = parts[2];
            return;
        }

        mUserId = null;
        mPublicKey = null;
    }

    @NonNull
    public final String toKey() {
        String address = mUserId + "/" + mPublicKey;
        String key = address.hashCode() + "/" + address;
        return Base64.encodeToString(key.getBytes(), Base64.URL_SAFE)
                .replaceAll("\n", "") // prevent base64 from breaking key on many keys
                .trim();
    }

    public String getUserId() {
        return mUserId;
    }

    public String getPublicAddress() {
        return mUserId + "/" + mPublicKey;
    }

    public String getTimetablePublicKey() {
        return mPublicKey;
    }

    /**
     * @return {@code true} if latest decode succeed and
     * contains both `user_key` and `timetable_public_key`,
     * {@code false} otherwise.
     */
    public boolean isCorrect() {
        return !TextUtils.isEmpty(mUserId) && !TextUtils.isEmpty(mPublicKey);
    }

}
