package com.artemchep.horario.ui.views;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.artemchep.basic.ui.MultiSelector;
import com.artemchep.basic.utils.Operator;
import com.artemchep.horario.R;
import com.artemchep.horario.aggregator.Aggregator;
import com.artemchep.horario.aggregator.Filter;
import com.artemchep.horario.models.Lesson;
import com.artemchep.horario.models.Subject;
import com.artemchep.horario.models.Teacher;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * @author Artem Chepurnoy
 */
public class WeekView extends FrameLayout implements
        View.OnClickListener,
        View.OnLongClickListener {

    @NonNull
    private final Comparator<Lesson> mLessonComparator = new Comparator<Lesson>() {
        @Override
        public int compare(Lesson o1, Lesson o2) {
            int i = o1.timeStart - o2.timeStart;
            if (i == 0) {
                i = o1.timeEnd - o2.timeEnd;
                if (i == 0) {
                    i = o1.key.compareTo(o2.key);
                }
            }
            return i;
        }
    };

    private long mTimeOffset;
    private long mTimeDelta;
    private Map<String, Teacher> mTeachers;
    private Map<String, Subject> mSubjects;

    private boolean mInitialized;
    private OnLessonClickListener mCallback;
    private Path mPaintPath;
    private Paint mPaint;

    private int mWeekNumber = 1;
    private int mWeekStartDay;

    private int mDay;
    private int mDaysMask;
    private int mDaysCount;
    private long mNow;
    private int mConfigTopPanelHeight;
    private int mConfigLeftPanelWidth;

    @NonNull
    private final BroadcastReceiver mTimeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Intent.ACTION_TIME_TICK:
                case Intent.ACTION_DATE_CHANGED:
                    updateCurrentTime();
                    break;
            }
        }
    };

    @NonNull
    private final Aggregator.Observer<Lesson> mObserver = new Aggregator.Observer<Lesson>() {

        private final SortedSetHelper<Integer> mSsh = new SortedSetHelper<>(new ArrayList<Integer>());

        @Override
        public void add(@NonNull Lesson model, int i) {
            WeekView self = WeekView.this;

            // Add day
            final int day = model.day;
            //noinspection StatementWithEmptyBody
            if (Operator.bitAnd(mDaysMask, 1 << day)) {
                // Lesson contains existing day, no
                // need to do anything.
            } else {
                mDaysMask |= 1 << day;
                mDaysCount += 1;

                DayView dv = (DayView) mInflater.inflate(R.layout.aa_view_day, self, false);
                dv.setDay(day);
                addView(dv);
            }

            // Add lesson
            LessonView lv = (LessonView) mInflater.inflate(R.layout.aa_view_lesson, self, false);
            lv.setOnLongClickListener(self);
            lv.setOnClickListener(self);
            lv.setLesson(model,
                    mSubjects.get(model.subject),
                    mTeachers.get(model.teacher));
            mMultiSelector.connect(lv, model.key);
            addView(lv, i);

            // Update frame
            if (model.timeStart < mTimeOffset) {
                mTimeDelta += mTimeOffset - model.timeStart;
                mTimeOffset = model.timeStart;
            }
            if (model.timeEnd > mTimeOffset + mTimeDelta) {
                mTimeDelta = model.timeEnd - mTimeOffset;
            }

            // Add time
            for (int time : new int[]{model.timeStart, model.timeEnd}) {
                int pos = mSsh.add(time);
                if (pos >= 0) {
                    BooView bv = (BooView) mInflater.inflate(R.layout.aa_view_time, self, false);
                    bv.setTime(time);
                    addView(bv);
                }
            }
        }

        @Override
        public void set(@NonNull Lesson model, int k) {
            WeekView self = WeekView.this;

            // Set lesson
            Lesson old;
            {
                final LessonView v = (LessonView) getChildAt(k);
                old = v.getLesson();
                v.setLesson(model, mSubjects.get(model.subject), mTeachers.get(model.teacher));
            }

            // Update day
            if (old.day != model.day) {
                boolean oldHasToBeRemoved = true;
                for (Lesson lesson : mFilterManager.getModels()) {
                    if (old.day == lesson.day) {
                        oldHasToBeRemoved = false;
                        break;
                    }
                }

                DayView v = null;
                if (oldHasToBeRemoved) {
                    final int size = getChildCount();
                    for (int i = 0; i < size; i++) {
                        View view = getChildAt(i);
                        if (view instanceof DayView) {
                            DayView w = (DayView) view;
                            if (w.getDay() == old.day) {
                                v = w;
                                break;
                            }
                        }
                    }

                    mDaysMask &= ~(1 << old.day);
                    mDaysCount -= 1;
                }

                final int day = model.day;
                if (Operator.bitAnd(mDaysMask, 1 << day)) {
                } else {
                    mDaysMask |= 1 << day;
                    mDaysCount += 1;

                    if (v == null) {
                        v = (DayView) mInflater.inflate(R.layout.aa_view_day, self, false);
                        addView(v);
                    }
                    v.setDay(day);
                    v = null;
                }

                if (v != null) {
                    removeView(v);
                }
            }

            // Update time
            long min = Long.MAX_VALUE;
            long max = Long.MIN_VALUE;
            for (Lesson lesson : mFilterManager.getModels()) {
                if (max < lesson.timeEnd) {
                    max = lesson.timeEnd;
                }
                if (min > lesson.timeStart) {
                    min = lesson.timeStart;
                }
            }
            mTimeOffset = min;
            mTimeDelta = max - min;

            updateRemoveTimeView(new int[]{
                    old.timeStart,
                    old.timeEnd,
            });

            for (int time : new int[]{
                    model.timeStart,
                    model.timeEnd,
            }) {
                int pos = mSsh.add(time);
                if (pos >= 0) {
                    BooView vb = (BooView) mInflater.inflate(R.layout.aa_view_time, self, false);
                    vb.setTime(time);
                    addView(vb);
                }
            }
        }

        @Override
        public void remove(@NonNull Lesson model, int k) {
            // Remove lesson
            Lesson old;
            {
                LessonView v = (LessonView) getChildAt(k);
                old = v.getLesson();
                removeViewAt(k);

                mMultiSelector.disconnect(v);
            }

            // Remove day (maybe)
            {
                boolean oldHasToBeRemoved = true;
                for (Lesson lesson : mFilterManager.getModels()) {
                    if (old.day == lesson.day) {
                        oldHasToBeRemoved = false;
                        break;
                    }
                }

                if (oldHasToBeRemoved) {
                    final int size = getChildCount();
                    for (int i = 0; i < size; i++) {
                        View view = getChildAt(i);
                        if (view instanceof DayView) {
                            DayView w = (DayView) view;
                            if (w.getDay() == old.day) {
                                removeView(w);
                                break;
                            }
                        }
                    }

                    mDaysMask &= ~(1 << old.day);
                    mDaysCount -= 1;
                }
            }

            // Update time
            long min = Long.MAX_VALUE;
            long max = Long.MIN_VALUE;
            for (Lesson lesson : mFilterManager.getModels()) {
                if (max < lesson.timeEnd) {
                    max = lesson.timeEnd;
                }
                if (min > lesson.timeStart) {
                    min = lesson.timeStart;
                }
            }
            mTimeOffset = min;
            mTimeDelta = max - min;

            updateRemoveTimeView(new int[]{
                    old.timeStart,
                    old.timeEnd,
            });
        }

        @NonNull
        private List<BooView> updateRemoveTimeView(int[] times) {
            List<BooView> list = new ArrayList<>();
            for (int time : times) {
                int pos = mSsh.remove(time);
                if (pos >= 0) {

                    int size = getChildCount();
                    for (int i = 0; i < size; i++) {
                        View view = getChildAt(i);
                        if (view instanceof BooView) {
                            BooView v = (BooView) view;
                            if (v.getTime() == time) {
                                list.add(v);
                                removeViewAt(i);
                                break;
                            }
                        }
                    }

                }
            }
            return list;
        }

        @Override
        public void move(@NonNull Lesson model, int from, int to) {
            throw new RuntimeException();
        }

        @Override
        public void avalanche() {
            WeekView self = WeekView.this;

            // Update time
            long min = Long.MAX_VALUE;
            long max = Long.MIN_VALUE;
            for (Lesson lesson : mFilterManager.getModels()) {
                if (max < lesson.timeEnd) {
                    max = lesson.timeEnd;
                }
                if (min > lesson.timeStart) {
                    min = lesson.timeStart;
                }
            }
            mTimeOffset = min;
            mTimeDelta = max - min;

            // Update days info
            int days = 0;
            for (Lesson lesson : mFilterManager.getModels()) {
                days |= 1 << lesson.day;
            }
            mDaysMask = days;
            mDaysCount = Integer.bitCount(days);

            // Update views
            int i = 0;
            int j = 0;
            int size = getChildCount();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            for (; i < mFilterManager.getModels().size(); i++) {
                LessonView v = null;
                Lesson lesson = mFilterManager.getModels().get(i);
                for (; j < size; j++) {
                    View view = getChildAt(j);
                    if (view instanceof LessonView) {
                        v = (LessonView) view;
                        removeViewAt(j);
                        j++;
                        break;
                    }
                }
                if (v == null) {
                    v = (LessonView) inflater.inflate(R.layout.aa_view_lesson, self, false);
                    v.setOnClickListener(self);
                    v.setOnLongClickListener(self);
                }
                addView(v, i);
                v.setLesson(lesson, mSubjects.get(lesson.subject), mTeachers.get(lesson.teacher));

                mMultiSelector.connect(v, lesson.key);
            }
            for (int k = size - 1; k >= j; k--) {
                View view = getChildAt(k);
                if (view instanceof LessonView) {
                    mMultiSelector.disconnect(view);
                    removeViewAt(k);
                    size--;
                }
            }
            // Add day views
            i = 0;
            j = 0;
            for (i = 0; i < 7; i++) {
                if (Operator.bitAnd(days, 1 << i)) {
                    DayView v = null;
                    Integer time = i;
                    for (; j < size; j++) {
                        View view = getChildAt(j);
                        if (view instanceof DayView) {
                            v = (DayView) view;
                            j++;
                            break;
                        }
                    }
                    if (v == null) {
                        v = (DayView) inflater.inflate(R.layout.aa_view_day, self, false);
                        addView(v);
                    }
                    v.setDay(time);
                }
            }
            for (int k = size - 1; k >= j; k--) {
                View view = getChildAt(k);
                if (view instanceof DayView) {
                    removeViewAt(k);
                }
            }


            mSsh.clear();
            for (Lesson lesson : mFilterManager.getModels()) {
                mSsh.add(lesson.timeStart);
                mSsh.add(lesson.timeEnd);
            }

            i = 0;
            j = 0;
            size = getChildCount();
            for (; i < mSsh.getList().size(); i++) {
                BooView v = null;
                Integer time = mSsh.getList().get(i);
                for (; j < size; j++) {
                    View view = getChildAt(j);
                    if (view instanceof BooView) {
                        v = (BooView) view;
                        j++;
                        break;
                    }
                }
                if (v == null) {
                    v = (BooView) mInflater.inflate(R.layout.aa_view_time, self, false);
                    addView(v);
                }
                v.setTime(time);
            }
            for (int k = size - 1; k >= j; k--) {
                View view = getChildAt(k);
                if (view instanceof BooView) {
                    removeViewAt(k);
                }
            }
        }

    };

    private Filter<Lesson> mFilter;

    @NonNull
    private final Aggregator<Lesson> mFilterManager = new Aggregator<>(new Filter<Lesson>() {
        @Override
        public boolean isValid(@NonNull Lesson model) {
            return (model.week == 0 || model.week == mWeekNumber)
                    && (mFilter == null || mFilter.isValid(model));
        }
    }, mLessonComparator, mObserver);

    MultiSelector<String> mMultiSelector = new MultiSelector<>();
    private boolean mMultiSelectEnabled = true;
    private LayoutInflater mInflater;

    /**
     * @author Artem Chepurnoy
     */
    public interface OnLessonClickListener {

        void onLessonClick(@NonNull View view, @NonNull Lesson lesson);

    }

    public WeekView(Context context) {
        super(context);
        init();
    }

    public WeekView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WeekView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        if (mInitialized) {
            return;
        } else mInitialized = true;

        mInflater = LayoutInflater.from(getContext());

        float density = getResources().getDisplayMetrics().density;
        mConfigTopPanelHeight = (int) (36 * density);
        mConfigLeftPanelWidth = (int) (48 * density);

        mPaint = new Paint();
        mPaint.setColor(0xFF7F7F7F);
        mPaint.setStrokeWidth(4 * density);
        mPaint.setStyle(Paint.Style.FILL);
      //  mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaintPath = new Path();
    }

    @Override
    public void onClick(View v) {
        if (mCallback == null) {
            return;
        } else if (v instanceof LessonView) {
            LessonView lv = (LessonView) v;
            if (mMultiSelector.isEmpty()) {
                mCallback.onLessonClick(v, lv.getLesson());
            } else mMultiSelector.toggle(lv.getLesson().key);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (v instanceof LessonView) {
            LessonView lv = (LessonView) v;
            String key = lv.getLesson().key;

            if (mMultiSelector.contains(key)) {
                // Do not perform long click.
                return false;
            } else if (mMultiSelectEnabled) {
                mMultiSelector.add(lv.getLesson().key);
                return true;
            }
        }

        return false;
    }

    public void setMultiSelectEnabled(boolean enabled) {
        mMultiSelectEnabled = enabled;
    }

    public void setWeekNumber(int weekNumber) {
        if (mWeekNumber == weekNumber) {
            // Avoid an expensive unnecessary re-filter
            // procedure.
            return;
        }

        mWeekNumber = weekNumber;
        mFilterManager.refilter(true);
    }

    public void setFilter(Filter<Lesson> filter) {
        mFilter = filter;
        mFilterManager.refilter();
    }

    public void setOnLessonClickListener(@Nullable OnLessonClickListener callback) {
        mCallback = callback;
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        drawing_current_time1:
        if (mTimeDelta != 0 && mDaysCount != 0) {
            //get the available size of child view
            final int left = getPaddingLeft();
            final int right = getMeasuredWidth() - getPaddingRight();
            final int childWidth = right - left;

            final int rectWidth = childWidth - mConfigLeftPanelWidth;
            final int widthLv = rectWidth / mDaysCount;

            int dayPos = Integer.bitCount(mDaysMask & ((1 << mDay) - 1));
            int vLeft = left + mConfigLeftPanelWidth + widthLv * dayPos;
            mPaint.setAlpha(20);
            canvas.drawRect(vLeft, 0, vLeft + widthLv, getHeight(), mPaint);
        }

        super.dispatchDraw(canvas);

        drawing_current_time:
        if (mTimeDelta != 0 && mDaysCount != 0) {
            if (mNow < mTimeOffset || mNow > mTimeOffset + mTimeDelta) {
                break drawing_current_time;
            }
            //get the available size of child view
            final int left = getPaddingLeft();
            final int top = getPaddingTop();
            final int right = getMeasuredWidth() - getPaddingRight();
            final int bottom = getMeasuredHeight() - getPaddingBottom();
            final int childWidth = right - left;
            final int childHeight = bottom - top;

            final int rectWidth = childWidth - mConfigLeftPanelWidth;
            final int widthLv = rectWidth / mDaysCount;

            int dayPos = Integer.bitCount(mDaysMask & ((1 << mDay) - 1));

            float height = getHeight() - mConfigTopPanelHeight;
            float width = getWidth();
            float time = Math.max(Math.min(mNow, mTimeOffset + mTimeDelta), mTimeOffset);
            float y = mConfigTopPanelHeight + (int) (height * (-mTimeOffset + time) / mTimeDelta);
            int vLeft = left + mConfigLeftPanelWidth + widthLv * dayPos;
            mPaintPath.reset();
            mPaintPath.moveTo(vLeft, y);
            mPaintPath.lineTo(vLeft + widthLv, y);
            mPaint.setAlpha(255);
            canvas.drawCircle(vLeft, y, mPaint.getStrokeWidth(), mPaint);
            mPaint.setStyle(Paint.Style.STROKE);
            canvas.drawPath(mPaintPath, mPaint);
            mPaint.setStyle(Paint.Style.FILL);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int count = getChildCount();
        int curWidth, curHeight, curLeft, curTop, maxHeight;

        //get the available size of child view
        final int left = getPaddingLeft();
        final int top = getPaddingTop();
        final int right = getMeasuredWidth() - getPaddingRight();
        final int bottom = getMeasuredHeight() - getPaddingBottom();
        final int childWidth = right - left;
        final int childHeight = bottom - top;

        final int rectHeight = childHeight - mConfigTopPanelHeight;
        final int rectWidth = childWidth - mConfigLeftPanelWidth;

        if (mDaysCount == 0) {
            return;
        }

        final int widthLv = rectWidth / mDaysCount;

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                return;
            } else if (child instanceof LessonView) {
                LessonView v = (LessonView) child;
                Lesson lesson = v.getLesson();
                int dayPos = Integer.bitCount(mDaysMask & ((1 << lesson.day) - 1));

                int vWidth = widthLv;
                int vHeight = Math.round(rectHeight * (lesson.timeEnd - lesson.timeStart) / mTimeDelta);
                int vLeft = left + mConfigLeftPanelWidth + widthLv * dayPos;
                int vTop = top + mConfigTopPanelHeight + (int) (rectHeight * (-mTimeOffset + lesson.timeStart) / mTimeDelta);

                child.measure(
                        MeasureSpec.makeMeasureSpec(vWidth, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(vHeight, MeasureSpec.EXACTLY));
                child.layout(vLeft, vTop, vLeft + vWidth, vTop + vHeight);
            } else if (child instanceof BooView) {
                BooView v = (BooView) child;
                int time = v.getTime();

                // Restrict view width, but allow it any height
                child.measure(
                        MeasureSpec.makeMeasureSpec(mConfigLeftPanelWidth, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(rectHeight, MeasureSpec.AT_MOST));

                int vWidth = mConfigLeftPanelWidth;
                int vHeight = child.getMeasuredHeight();
                //noinspection UnnecessaryLocalVariable
                int vLeft = left;
                int vTop = top + mConfigTopPanelHeight + (int) (rectHeight * (-mTimeOffset + time) / mTimeDelta) - vHeight / 2;

                int vTopMin = top + mConfigTopPanelHeight;
                int vTopMax = top + mConfigTopPanelHeight + rectHeight - vHeight;
                vTop = Math.min(Math.max(vTop, vTopMin), vTopMax);

                child.layout(vLeft, vTop, vLeft + vWidth, vTop + vHeight);
            } else if (child instanceof DayView) {
                DayView v = (DayView) child;
                int day = v.getDay();
                int dayPos = Integer.bitCount(mDaysMask & ((1 << day) - 1));

                //noinspection UnnecessaryLocalVariable
                int vWidth = widthLv;
                int vHeight = mConfigTopPanelHeight;
                int vLeft = left + mConfigLeftPanelWidth + widthLv * dayPos;
                //noinspection UnnecessaryLocalVariable
                int vTop = top;

                child.measure(
                        MeasureSpec.makeMeasureSpec(vWidth, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(vHeight, MeasureSpec.EXACTLY));
                child.layout(vLeft, vTop, vLeft + vWidth, vTop + vHeight);
            }
        }
    }

    private int convertDay(int day) {
        day -= mWeekStartDay;
        if (day < 0) {
            day += 7;
        }
        return 0;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        getContext().registerReceiver(mTimeReceiver, filter);
        updateCurrentTime();
    }

    @Override
    protected void onDetachedFromWindow() {
        getContext().unregisterReceiver(mTimeReceiver);
        super.onDetachedFromWindow();
    }

    public void setTeachers(Map<String, Teacher> map) {
        mTeachers = map;
    }

    public void setSubjects(Map<String, Subject> map) {
        mSubjects = map;
    }

    @NonNull
    public final MultiSelector<String> getMultiSelector() {
        return mMultiSelector;
    }

    @NonNull
    public final Aggregator<Lesson> getFilterManager() {
        return mFilterManager;
    }

    /**
     * @return current week number, by default {@code 1}.
     * @see #setWeekNumber(int)
     */
    public final int getWeekNumber() {
        return mWeekNumber;
    }

    /**
     * Call this when lesson (or its subject/teacher) has changed and
     * needs to be updated visually.
     */
    public void notifyLessonChanged(@NonNull Lesson lesson) {
        if (lesson.week != 0 && lesson.week != mWeekNumber) {
            // This lesson is not being shown.
            return;
        }

        final int size = getChildCount();
        for (int i = 0; i < size; i++) {
            View child = getChildAt(i);
            if (child instanceof LessonView) {
                LessonView v = (LessonView) child;
                Lesson l = v.getLesson();

                if (l.key.equals(lesson.key)) {
                    v.setLesson(lesson,
                            mSubjects.get(lesson.subject),
                            mTeachers.get(lesson.teacher));
                }
            }
        }
    }

    private void updateCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        int h = calendar.get(Calendar.HOUR_OF_DAY);
        int m = calendar.get(Calendar.MINUTE);

        int now = h * 60 + m + 1 /* see Lesson.Time */;
        if (now != mNow) {
            mNow = now;
            mDay = calendar.get(Calendar.DAY_OF_WEEK);

            // Change to our format (week starts from Monday:0)
            // from their format (week starts from Sunday:1).
            mDay -= 2;
            if (mDay < 0) mDay += 7;

            postInvalidateOnAnimation();
        }
    }

    /**
     * @author Artem Chepurnoy
     */
    private static class SortedSetHelper<T extends Comparable<T>> {

        @NonNull
        private final List<Integer> mList;
        @NonNull
        private final SparseIntArray mSparse;

        SortedSetHelper(@NonNull List<Integer> list) {
            mList = list;
            mSparse = new SparseIntArray();
        }

        public int add(@NonNull Integer a) {
            int i = Collections.binarySearch(mList, a, null);
            if (i < 0) {
                int pos = -i - 1;
                mList.add(pos, a);
                mSparse.put(a, 1);
                return pos;
            }

            int v = mSparse.get(a);
            mSparse.put(a, v + 1);
            return -1;
        }

        public int remove(@NonNull Integer a) {
            int i = Collections.binarySearch(mList, a, null);
            if (i < 0) {
                return -1;
            }

            int v = mSparse.get(a);
            if (v == 1) {
                mSparse.delete(a);
                mList.remove(a);
                return i;
            } else if (v > 1) {
                mSparse.put(a, v - 1);
            }
            return -1;
        }

        public void clear() {
            mSparse.clear();
            mList.clear();
        }

        @NonNull
        public final List<Integer> getList() {
            return mList;
        }

    }

}
