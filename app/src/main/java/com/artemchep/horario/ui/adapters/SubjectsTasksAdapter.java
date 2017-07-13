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
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.artemchep.horario.R;
import com.artemchep.horario.database.models.SubjectTask;
import com.artemchep.horario.ui.fragments.master.ModelFragment;
import com.artemchep.horario.ui.widgets.UserView;
import com.artemchep.horario.utils.DateUtilz;

import java.util.Calendar;
import java.util.List;

/**
 * @author Artem Chepurnoy
 */
public class SubjectsTasksAdapter extends ModelFragment.BaseAdapter<SubjectTask> {

    /**
     * @author Artem Chepurnoy
     */
    private static class ViewHolder extends ModelFragment.BaseHolder<SubjectTask> {

        final TextView statusTextView;
        final TextView titleTextView;
        final TextView decrTextView;
        final UserView userView;

        final View dueView;
        final TextView dueTextView;

        ViewHolder(@NonNull View v, @NonNull ModelFragment<SubjectTask> fragment) {
            super(v, fragment);
            v.findViewById(R.id.more).setOnClickListener(this);

            userView = (UserView) v.findViewById(R.id.uss);
            statusTextView = (TextView) v.findViewById(R.id.status);
            titleTextView = (TextView) v.findViewById(R.id.title);
            decrTextView = (TextView) v.findViewById(R.id.description);
            dueView = v.findViewById(R.id.due);
            dueTextView = (TextView) v.findViewById(R.id.due_text);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.more:
                    break;
                default:
                    super.onClick(v);
            }
        }

    }

    private final String[] mMonths;
    private final int mCurrentYear;

    public SubjectsTasksAdapter(
            @NonNull ModelFragment<SubjectTask> fragment,
            @NonNull List<SubjectTask> list) {
        super(fragment, list);
        mMonths = fragment.getResources().getStringArray(R.array.months);
        mCurrentYear = Calendar.getInstance().get(Calendar.YEAR);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View v = LayoutInflater.from(context).inflate(R.layout.item_assignment, parent, false);
        return new ViewHolder(v, getFragment());
    }

    @Override
    public void onBindViewHolder(ModelFragment.BaseHolder<SubjectTask> holder, int position) {
        super.onBindViewHolder(holder, position);
        ViewHolder h = (ViewHolder) holder;
        SubjectTask subject = getItem(position);
        Resources res = getFragment().getResources();

        h.userView.setUser(subject.author);
        h.titleTextView.setText(subject.title);

        String type;
        switch (subject.type) {
            case SubjectTask.TYPE_QUESTION:
                type = "Question";
                break;
            case SubjectTask.TYPE_ASSIGNMENT:
                type = "Assignment";
                break;
            case SubjectTask.TYPE_ANNOUNCEMENT:
            default:
                type = "Announcement";
                break;
        }
        h.statusTextView.setText(type + "; April 20, 18:50");

        // Bind description view
        if (!TextUtils.isEmpty(subject.description)) {
            h.decrTextView.setText(subject.description);
            h.decrTextView.setVisibility(View.VISIBLE);
        } else h.decrTextView.setVisibility(View.GONE);

        // Bind due-time view
        if (subject.due > 0) {
            int y, m, d;
            // Select previously selected day
            y = DateUtilz.getYear(subject.due);
            m = DateUtilz.getMonth(subject.due);
            d = DateUtilz.getDay(subject.due);

            if (y != mCurrentYear) {
                h.dueTextView.setText(res.getString(
                        R.string.subject_stream_task_due_year,
                        mMonths[m], d, y));
            } else h.dueTextView.setText(res.getString(
                    R.string.subject_stream_task_due,
                    mMonths[m], d));

            h.dueView.setVisibility(View.VISIBLE);
        } else h.dueView.setVisibility(View.GONE);
    }

}
