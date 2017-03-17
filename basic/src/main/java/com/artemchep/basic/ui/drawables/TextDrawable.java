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
package com.artemchep.basic.ui.drawables;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextPaint;

/**
 * @author Artem Chepurnoy
 */
public class TextDrawable extends Drawable {

    private final TextPaint mPaint;

    @Nullable
    private String mText;

    private float mTransY;

    public TextDrawable() {
        mPaint = new TextPaint();
        mPaint.setColor(Color.GRAY);
        mPaint.setFakeBoldText(true);
        mPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setTranslationY(float transY) {
        mTransY = transY;
        invalidateSelf();
    }

    public void setTextSize(float textSize) {
        mPaint.setTextSize(textSize);
        invalidateSelf();
    }

    public void setText(@Nullable String text) {
        mText = text;
        invalidateSelf();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void draw(@NonNull Canvas canvas) {
        if (mText != null) {
            float xPos = getBounds().centerX();
            float yPos = getBounds().centerY() - ((mPaint.descent() + mPaint.ascent()) / 2) + mTransY;
            canvas.drawText(mText, xPos, yPos, mPaint);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
        invalidateSelf();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setColorFilter(@Nullable ColorFilter cf) {
        mPaint.setColorFilter(cf);
        invalidateSelf();
    }

    /**
     * Specify an optional color filter for the drawable. Note that the color
     * is an int containing alpha as well as r,g,b. This 32bit value is not
     * pre-multiplied, meaning that its alpha can be any value, regardless
     * of the values of r,g,b. See the {@link Color Color class} for more details.
     *
     * @param color the color to be set
     * @see #setColorFilter(ColorFilter)
     */
    public void setColor(int color) {
        mPaint.setColor(color);
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
