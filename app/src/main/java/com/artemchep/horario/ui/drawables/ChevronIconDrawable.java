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
package com.artemchep.horario.ui.drawables;

import com.artemchep.basic.ui.drawables.TransformationDrawable;

/**
 * @author Artem Chepurnoy
 */
public class ChevronIconDrawable extends TransformationDrawable {

    public static final int POINTING_RIGHT = 0;
    public static final int POINTING_LEFT = 1;

    private static final float[][] VERTEX_NORMAL = {
            {80f / 192f, 80f / 192f, 119f / 192f},
            {56f / 192f, 135f / 192f, 95.5f / 192f}
    };

    private static final float[][] VERTEX_UPSIDE_DOWN = {
            {111f / 192f, 111f / 192f, 72f / 192f},
            {56f / 192f, 135f / 192f, 95.5f / 192f}
    };

    public ChevronIconDrawable() {
        super(VERTEX_NORMAL, VERTEX_UPSIDE_DOWN);
    }

    public void transformToRight() {
        transformToShape(POINTING_RIGHT);
    }

    public void transformToLeft() {
        transformToShape(POINTING_LEFT);
    }

}
