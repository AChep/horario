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
package com.artemchep.horario.ui.drawables

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt

/**
 * @author Artem Chepurnoy
 */
class CircleDrawable : Drawable() {

    /**
     * Setting color overrides alpha bits.
     */
    var color: Int
        get() = paint.color
        set(@ColorInt color) {
            paint.color = color or -0x1000000 // ignore alpha bits
            invalidateSelf()
        }

    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    /**
     * {@inheritDoc}
     */
    override fun draw(canvas: Canvas) {
        val rect = bounds
        canvas.drawCircle(
                rect.exactCenterX(),
                rect.exactCenterY(),
                Math.min(rect.height(), rect.width()) / 2f, paint)
    }

    /**
     * {@inheritDoc}
     */
    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
        invalidateSelf()
    }

    /**
     * {@inheritDoc}
     */
    override fun setColorFilter(cf: ColorFilter?) {
        paint.colorFilter = cf
        invalidateSelf()
    }

    /**
     * {@inheritDoc}
     */
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

}
