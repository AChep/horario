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
package com.artemchep.horario.ui.bitmaps;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * @author Artem Chepurnoy
 */
public class AvatarFactory {

    private static final int SIZE = 32; // px
    private static final int STEP = 8;

    public static Bitmap create(int seed) {
        Bitmap bitmap = Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);

        int i = 0, s = 0;
        for (int x = 0; x < SIZE; x += STEP) {
            for (int y = 0; y < SIZE; y += STEP) {
                i = i + 1;
                s = (seed >>> i) % 25;
                s = 100 + Math.abs(s * 4);
                paint.setColor(Color.argb(255, s, s, s));
                canvas.drawRect(x, y, x + STEP, y + STEP, paint);
            }
        }
        return bitmap;
    }

}
