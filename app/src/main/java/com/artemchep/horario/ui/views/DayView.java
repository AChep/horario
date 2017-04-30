package com.artemchep.horario.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.RectF;
import android.os.Build;
import android.support.v7.widget.AppCompatTextView;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.method.TransformationMethod;
import android.util.AttributeSet;

import com.artemchep.horario.R;

/**
 * @author Artem Chepurnoy
 */
public class DayView extends AppCompatTextView {

    private static final int NO_LINE_LIMIT = -1;

    private int mDay;

    private String[] mDayLabels;
    private String[] mDayLabelsShort;
    private SizeTester mSizeTester;
    private TextPaint mPaint;

    private boolean mInitialized;

    private interface SizeTester {

        /**
         * @param suggestedSize  Size of text to be tested
         * @param availableSpace available space in which text must fit
         * @return an integer < 0 if after applying {@code suggestedSize} to
         * text, it takes less space than {@code availableSpace}, > 0
         * otherwise
         */
        int onTestSize(int suggestedSize, RectF availableSpace);

    }

    public DayView(Context context) {
        super(context);
        init();
    }

    public DayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        if (mInitialized) {
            return;
        } else mInitialized = true;

        mDayLabels = getResources().getStringArray(R.array.days);
        mDayLabelsShort = getResources().getStringArray(R.array.days_short);

        setMaxLines(1);

        mPaint = new TextPaint(getPaint());
        mSizeTester = new SizeTester() {
            final RectF textRect = new RectF();

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public int onTestSize(final int suggestedSize, final RectF availableSpace) {
                mPaint.setTextSize(suggestedSize);
                final TransformationMethod transformationMethod = getTransformationMethod();
                final String text;

                if (transformationMethod != null) {
                    text = transformationMethod.getTransformation(getText(), DayView.this).toString();
                } else {
                    text = getText().toString();
                }

                final boolean singleLine = getMaxLines() == 1;
                if (singleLine) {
                    textRect.bottom = mPaint.getFontSpacing();
                    textRect.right = mPaint.measureText(text);
                } else {
                    final StaticLayout layout = new StaticLayout(text, mPaint,
                            (int) availableSpace.width(), Layout.Alignment.ALIGN_NORMAL,
                            getLineSpacingMultiplier(), getLineSpacingExtra(), true);
                    // return early if we have more lines
                    if (getMaxLines() != NO_LINE_LIMIT && layout.getLineCount() > getMaxLines())
                        return 1;
                    textRect.bottom = layout.getHeight();
                    int maxWidth = -1;
                    int lineCount = layout.getLineCount();
                    for (int i = 0; i < lineCount; i++) {
                        int end = layout.getLineEnd(i);
                        if (i < lineCount - 1 && end > 0 && !isValidWordWrap(text.charAt(end - 1), text.charAt(end)))
                            return 1;
                        if (maxWidth < layout.getLineRight(i) - layout.getLineLeft(i))
                            maxWidth = (int) layout.getLineRight(i) - (int) layout.getLineLeft(i);
                    }

                    textRect.right = maxWidth;
                }
                textRect.offsetTo(0, 0);
                if (availableSpace.contains(textRect))
                    // may be too small, don't worry we will find the best match
                    return -1;
                // else, too big
                return 1;
            }
        };
    }

    public boolean isValidWordWrap(char before, char after) {
        return before == ' ' || before == '-';
    }

    public void setDay(int time) {
        mDay = time;
        updateText();
    }

    public int getDay() {
        return mDay;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w != oldw || h != oldh) {
           //updateText();
        }
    }

    private void updateText() {
        setText(mDayLabels[mDay]);
/*
        RectF space = new RectF();
        space.right = getMeasuredWidth() - getCompoundPaddingLeft() - getCompoundPaddingRight();
        space.bottom = getMeasuredHeight() - getCompoundPaddingBottom() - getCompoundPaddingTop();
        if (mSizeTester.onTestSize((int) getTextSize(), space) > 0) {
            setText(mDayLabelsShort[mDay]);
        }*/
    }

}
