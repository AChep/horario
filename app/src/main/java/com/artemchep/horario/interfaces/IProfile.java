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
package com.artemchep.horario.interfaces;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

/**
 * @author Artem Chepurnoy
 */
public interface IProfile {

    /**
     * @return the name of this profile.
     * @see #getIcon()
     */
    @NonNull
    String getName();

    /**
     * @return drawable resource or {@code 0} to clear one
     * @see #getName()
     */
    @DrawableRes
    int getIcon();

    /**
     * Id must be unique only if profile is {@link #isSelectable() selectable},
     * otherwise this value is not used at all.
     *
     * @return the id of this profile
     * @see #isSelectable()
     */
    int getId();

    /**
     * @return {@code true} if user selects this item by clicking on it,
     * {@code false} otherwise.
     * @see #getId()
     */
    boolean isSelectable();

}
