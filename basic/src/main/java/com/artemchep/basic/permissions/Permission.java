/*
 * Copyright (C) 2017 Artem Chepurnoy <artemchep@gmail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
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
package com.artemchep.basic.permissions;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * @author Artem Chepurnoy
 */
public class Permission {

    @NonNull
    private final String mPermission;

    public Permission(@NonNull String permission) {
        mPermission = permission;
    }

    /**
     * @return {@code true} if application can request this permission
     * from user, {@code false} otherwise.
     */
    public boolean isAllowed(@NonNull Context context) {
        return true;
    }

    /**
     * @return {@code true} if application may request this permission,
     * {@code false} if it does not exist on this device.
     */
    public boolean exists(@NonNull Context context) {
        return true;
    }

}
