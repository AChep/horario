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
package com.artemchep.horario

import android.graphics.Color
import android.support.annotation.ColorInt

/**
 * @author Artem Chepurnoy
 */
object Palette {

    @ColorInt
    val UNKNOWN = -0xbaa59c

    @ColorInt
    val RED = -0xbbcca
    @ColorInt
    val PINK = -0x16e19d
    @ColorInt
    val PURPLE = -0x63d850
    @ColorInt
    val DEEP_PURPLE = -0x98c549
    @ColorInt
    val INDIGO = -0xc0ae4b
    @ColorInt
    val BLUE = -0xde690d
    @ColorInt
    val CYAN = -0xff6859
    @ColorInt
    val TEAL = -0xff6978
    @ColorInt
    val GREEN = -0xbc5fb9
    @ColorInt
    val LIGHT_GREEN = -0x743cb6
    @ColorInt
    val LIME = -0x3223c7
    @ColorInt
    val YELLOW = -0x14c5
    @ColorInt
    val AMBER = -0x3ef9
    @ColorInt
    val ORANGE = -0x6800
    @ColorInt
    val DEEP_ORANGE = -0xa8de
    @ColorInt
    val BROWN = -0x86aab8
    @ColorInt
    val GREY = -0x616162

    val PALETTE = intArrayOf(
            RED, PINK, PURPLE, DEEP_PURPLE,
            INDIGO, BLUE, CYAN, TEAL, GREEN,
            LIGHT_GREEN, LIME, YELLOW, AMBER,
            ORANGE, DEEP_ORANGE, BROWN, GREY)

    /**
     * @return closest material color
     */
    @ColorInt
    fun findColorByHue(colors: IntArray, @ColorInt color: Int): Int {
        if (colors.isEmpty()) throw IllegalArgumentException("Must be not empty!")

        // Convert color to HSV format.
        val hsv = FloatArray(3)
        val tmp = FloatArray(3)
        Color.colorToHSV(color, hsv)

        var diff = java.lang.Float.POSITIVE_INFINITY
        var choice = PALETTE[0]
        for (c in colors) {
            Color.colorToHSV(c, tmp)
            // Calculate hue diff
            var dHue = Math.abs(tmp[0] - hsv[0]) / 360f
            if (dHue > 0.5f) dHue = 1f - dHue
            // Calculate saturation diff
            val dSat = Math.abs(tmp[1] - hsv[1])
            // Calculate value diff
            val dVal = Math.abs(tmp[2] - hsv[2])

            val d = dHue + dSat / 3f + dVal / 3f
            if (diff > d) {
                choice = c
                diff = d
            }
        }

        return choice
    }

}
