package com.artemchep.horario.ui.views;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.artemchep.horario.utils.DateUtilz;

/**
 * @author Artem Chepurnoy
 */
public class BooView extends AppCompatTextView {

    private int mTime;

    public BooView(Context context) {
        super(context);
    }

    public BooView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BooView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setTime(int time) {
        mTime = time;
        setText(DateUtilz.formatLessonTime(time));
    }

    public int getTime() {
        return mTime;
    }

}
