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

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

/**
 * @author Artem Chepurnoy
 */
public class AvatarDrawable extends Drawable {

    private static final int STEP = 8;

    private final Paint mPaint;

    private int mSeed;

    public AvatarDrawable() {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
    }

    public void setSeed(int seed) {
        mSeed = seed;
        invalidateSelf();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.drawColor(Color.BLACK);
        final Rect rect = getBounds();

        int step = rect.width() / STEP;
        int i = 0, s = 0;
        for (int x = rect.left; x < rect.right; x += step) {
            for (int y = rect.top; y < rect.bottom; y += step) {
                i = i + 1;
                s = (mSeed >>> i) % 25;
                s = 100 + Math.abs(s * 4);
                mPaint.setColor(Color.argb(255, s, s, s));
                canvas.drawRect(x, y, x + step, y + step, mPaint);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAlpha(int alpha) {
        throw new RuntimeException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
        invalidateSelf();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
