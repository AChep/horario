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

import android.graphics.Color;
import android.support.annotation.ColorInt;

/**
 * @author Artem Chepurnoy
 */
public class Palette {

    public static final int RED = 0xFFF44336;
    public static final int PINK = 0xFFE91E63;
    public static final int PURPLE = 0xFF9C27B0;
    public static final int DEEP_PURPLE = 0xFF673AB7;
    public static final int INDIGO = 0xFF3F51B5;
    public static final int BLUE = 0xFF2196F3;
    public static final int CYAN = 0xFF0097A7;
    public static final int TEAL = 0xFF009688;
    public static final int GREEN = 0xFF43A047;
    public static final int LIGHT_GREEN = 0xFF8BC34A;
    public static final int LIME = 0xFFCDDC39;
    public static final int YELLOW = 0xFFFFEB3B;
    public static final int AMBER = 0xFFFFC107;
    public static final int ORANGE = 0xFFFF9800;
    public static final int DEEP_ORANGE = 0xFFFF5722;
    public static final int BROWN = 0xFF795548;
    public static final int GREY = 0xFF9E9E9E;

    public static final int[] PALETTE = new int[]{
            RED, PINK, PURPLE,
            DEEP_PURPLE, INDIGO,
            BLUE, CYAN, TEAL,
            GREEN, LIGHT_GREEN,
            LIME, YELLOW, AMBER,
            ORANGE, DEEP_ORANGE,
            BROWN, GREY,
    };

    /**
     * @return closest material color
     */
    @ColorInt
    public static int findColorByHue(int[] colors, @ColorInt int color) {
        if (colors.length == 0) throw new IllegalArgumentException("Must be not empty!");

        // Convert color to HSV format.
        float[] hsv = new float[3];
        float[] tmp = new float[3];
        Color.colorToHSV(color, hsv);

        float diff = Float.POSITIVE_INFINITY;
        int choice = PALETTE[0];
        for (int c : colors) {
            Color.colorToHSV(c, tmp);
            // Calculate hue diff
            float dHue = Math.abs(tmp[0] - hsv[0]) / 360f;
            if (dHue > 0.5f) dHue = 1f - dHue;
            // Calculate saturation diff
            float dSat = Math.abs(tmp[1] - hsv[1]);
            // Calculate value diff
            float dVal = Math.abs(tmp[2] - hsv[2]);

            float d = dHue + dSat / 3f + dVal / 3f;
            if (diff > d) {
                choice = c;
                diff = d;
            }
        }

        return choice;
    }

}
