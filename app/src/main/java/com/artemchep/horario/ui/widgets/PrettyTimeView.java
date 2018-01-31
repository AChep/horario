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
package com.artemchep.horario.ui.widgets;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.artemchep.horario.tests.Check;

import org.ocpsoft.prettytime.Duration;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;

/**
 * @author Artem Chepurnoy
 */
public class PrettyTimeView extends AppCompatTextView {

    private Date mDate;
    private Duration mDuration;
    private PrettyTime mPrettyTime;

    private boolean mInitialized;
    private boolean mAttachedToWindow;

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            Duration d = mPrettyTime.approximateDuration(mDate);
            if (!d.equals(mDuration)) {
                mDuration = d;

                // Update description
                String text = mPrettyTime.format(d);
                if (mPrefix != null) {
                    text = mPrefix + text;
                }
                setText(text);
            }

            // Queue next update in 1 minute
            postDelayed(this, 1000 * 60); // refresh every minute
        }
    };

    private String mPrefix;

    public PrettyTimeView(Context context) {
        super(context);
        init();
    }

    public PrettyTimeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        if (mInitialized) {
            return;
        } else mInitialized = true;

        mDate = new Date(0);
        mPrettyTime = new PrettyTime();
    }

    public void setPrefix(@Nullable String prefix) {
        mPrefix = prefix;
    }

    public void setTime(long time) {
        Check.getInstance().isInMainThread();
        removeCallbacks(mRunnable);
        mDate.setTime(time);
        mDuration = null;

        if (mAttachedToWindow && time != 0) {
            post(mRunnable);
        } else setText(null);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mAttachedToWindow = true;

        if (mDate.getTime() != 0) {
            post(mRunnable);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(mRunnable);
        mAttachedToWindow = false;
    }

}
