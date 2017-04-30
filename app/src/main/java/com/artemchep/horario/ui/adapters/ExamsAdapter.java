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
package com.artemchep.horario.ui.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.artemchep.horario.R;
import com.artemchep.horario.models.Exam;
import com.artemchep.horario.models.Subject;
import com.artemchep.horario.models.Teacher;
import com.artemchep.horario.ui.UiHelper;
import com.artemchep.horario.ui.drawables.CircleDrawable;
import com.artemchep.horario.ui.fragments.master.ModelFragment;
import com.artemchep.horario.utils.CsUtils;
import com.artemchep.horario.utils.DateUtilz;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * @author Artem Chepurnoy
 */
public class ExamsAdapter extends ModelFragment.BaseAdapter<Exam> {

    /**
     * @author Artem Chepurnoy
     */
    public static class ViewHolderExam {

        @NonNull
        public final View itemView;

        public final View colorView;
        public final TextView titleTextView;
        public final TextView subtitleTextView;
        public final TextView datetimeTextView;

        public final CircleDrawable colorDrawable;

        public ViewHolderExam(@NonNull View v) {
            itemView = v;
            colorDrawable = new CircleDrawable();
            titleTextView = (TextView) v.findViewById(R.id.title);
            subtitleTextView = (TextView) v.findViewById(R.id.subtitle);
            datetimeTextView = (TextView) v.findViewById(R.id.datetime);
            colorView = v.findViewById(R.id.color);
            colorView.setBackground(colorDrawable);
        }

    }

    /**
     * @author Artem Chepurnoy
     */
    private static class ViewHolder extends ModelFragment.BaseHolder<Exam> {

        final ViewHolderExam holder;

        ViewHolder(@NonNull View v, @NonNull ModelFragment<Exam> fragment) {
            super(v, fragment);
            holder = new ViewHolderExam(v);
        }

    }

    private final String[] mMonths;
    private final int mNow;

    @NonNull
    private final Map<String, Subject> mSubjectMap;
    @NonNull
    private final Map<String, Teacher> mTeacherMap;

    public ExamsAdapter(
            @NonNull ModelFragment<Exam> fragment, @NonNull List<Exam> list,
            @NonNull Map<String, Subject> subjectMap,
            @NonNull Map<String, Teacher> teacherMap) {
        super(fragment, list);
        mSubjectMap = subjectMap;
        mTeacherMap = teacherMap;

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        mNow = DateUtilz.mergeDate(year, month, day);

        mMonths = fragment.getResources().getStringArray(R.array.months);
    }

    @Override
    public ModelFragment.BaseHolder<Exam> onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View v = LayoutInflater.from(context).inflate(R.layout.item_exam, parent, false);
        return new ViewHolder(v, getFragment());
    }

    @Override
    public void onBindViewHolder(ModelFragment.BaseHolder<Exam> holder, int position) {
        super.onBindViewHolder(holder, position);
        ViewHolderExam h = ((ViewHolder) holder).holder;
        Exam exam = getItem(position);

        Subject subject = mSubjectMap.get(exam.subject);
        if (subject != null) {
            h.titleTextView.setText(subject.name);
            h.colorDrawable.setColor(subject.color);
            h.colorView.setVisibility(View.VISIBLE);
        } else {
            h.titleTextView.setText(UiHelper.getPlaceholderText(exam.subject));
            h.colorView.setVisibility(View.INVISIBLE);
        }

        List<CharSequence> st = new ArrayList<>();
        Teacher teacher = mTeacherMap.get(exam.teacher);
        if (teacher != null) st.add(teacher.name);
        if (!TextUtils.isEmpty(exam.place)) st.add(exam.place);
        if (st.isEmpty()) {
            h.subtitleTextView.setVisibility(View.GONE);
        } else {
           CharSequence cs = CsUtils.join(";    ", st.toArray(new CharSequence[st.size()]));
            h.subtitleTextView.setVisibility(View.VISIBLE);
            h.subtitleTextView.setText(cs);
        }

        int day = DateUtilz.getDay(exam.date);
        int month = DateUtilz.getMonth(exam.date);
        TextView dt = h.datetimeTextView;
        if (exam.time != 0) {
            String time = DateUtilz.formatLessonTime(exam.time);
            dt.setText(mMonths[month] + " " + day + ", " + time);
        } else dt.setText(mMonths[month] + " " + day);

        int flags = dt.getPaintFlags();
        if (exam.date < mNow) {
            flags |= Paint.STRIKE_THRU_TEXT_FLAG;
        } else flags = flags & ~Paint.STRIKE_THRU_TEXT_FLAG;
        h.titleTextView.setPaintFlags(flags);
    }

}
