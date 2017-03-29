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

    public static final int[] PALETTE = new int[]{
            0xFFF44336, // red
            0xFFE91E63, // pink
            0xFF9C27B0, // purple
            0xFF673AB7, // deep purple
            0xFF3F51B5, // indigo
            0xFF2196F3, // blue
            0xFF0097A7, // cyan
            0xFF009688, // teal
            0xFF43A047, // green
            0xFF8BC34A, // light green
            0xFFCDDC39, // lime
            0xFFFFEB3B, // yellow
            0xFFFFC107, // amber
            0xFFFF9800, // orange
            0xFFFF5722, // deep orange
            0xFF795548, // brown
            0xFF9E9E9E, // grey
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
