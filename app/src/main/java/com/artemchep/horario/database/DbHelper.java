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

import android.support.annotation.NonNull;

import com.artemchep.horario.models.Timetable;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Artem Chepurnoy
 */
public class DbHelper {

    public static void removeTimetable(@NonNull String userId, @NonNull Timetable timetable) {
        removeTimetable(userId, timetable, true);
    }

    public static void removeTimetable(@NonNull String userId, @NonNull Timetable timetable, boolean removePublic) {
        DatabaseReference userRef = Db.user(userId).ref();
        // Delete timetable
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/timetable/" + timetable.key, null);
        childUpdates.put("/timetable_private/" + timetable.privateKey, null);
        if (removePublic) {
            Address address = Address.fromModel(timetable);
            assert address != null; // because the timetable is not null
            // Try to remove public timetable only if
            // user owns it.
            if (userId.equals(address.publicUserKey)) {
                childUpdates.put("/timetable_public/" + address.publicKey, null);
            }
        }
        userRef.updateChildren(childUpdates);
    }

}
