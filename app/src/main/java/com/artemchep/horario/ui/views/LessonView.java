package com.artemchep.horario.ui.views;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.artemchep.horario.R;
import com.artemchep.horario.models.Lesson;
import com.artemchep.horario.models.Subject;
import com.artemchep.horario.models.Teacher;
import com.thebluealliance.spectrum.internal.ColorUtil;

/**
 * @author Artem Chepurnoy
 */
public class LessonView extends FrameLayout {

    private ViewGroup mContainer;
    private TextView mNameTextView;
    private TextView mInfoTextView;
    private Lesson mLesson;

    public LessonView(Context context) {
        super(context);
    }

    public LessonView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LessonView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mContainer = (ViewGroup) findViewById(R.id.container);
        mNameTextView = (TextView) findViewById(R.id.name);
        mInfoTextView = (TextView) findViewById(R.id.info);
    }

    public void setLesson(
            @NonNull Lesson lesson, @Nullable Subject subject,
            @Nullable Teacher teacher) {
        mInfoTextView.setText(lesson.place);
        if (subject == null) {
            mNameTextView.setText(lesson.subject);
        } else {
            int textColor = ColorUtil.isColorDark(subject.color) ? Color.WHITE : Color.BLACK;
            mInfoTextView.setTextColor(textColor & 0x80FFFFFF);
            mNameTextView.setTextColor(textColor);
            mNameTextView.setText(!TextUtils.isEmpty(subject.abbreviation) ? subject.abbreviation : subject.name);
            setBackgroundColor(subject.color);
        }
        mLesson = lesson;
    }

    public Lesson getLesson() {
        return mLesson;
    }
}
