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
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.artemchep.horario.Palette;
import com.artemchep.horario.R;
import com.artemchep.horario.models.Absence;
import com.artemchep.horario.models.Subject;
import com.artemchep.horario.ui.UiHelper;
import com.artemchep.horario.ui.drawables.CircleDrawable;
import com.artemchep.horario.ui.fragments.master.ModelFragment;
import com.artemchep.horario.utils.DateUtilz;

import java.util.List;
import java.util.Map;

/**
 * @author Artem Chepurnoy
 */
public class AttendanceAdapter extends ModelFragment.BaseAdapter<Absence> {

    /**
     * @author Artem Chepurnoy
     */
    private static class ViewHolder extends ModelFragment.BaseHolder<Absence> {

        final View avatarView;
        final CircleDrawable avatarDrawable;
        final TextView titleTextView;
        final TextView subtitleTextView;

        ViewHolder(@NonNull View v, @NonNull ModelFragment<Absence> fragment) {
            super(v, fragment);
            avatarDrawable = new CircleDrawable();
            avatarView = v.findViewById(R.id.color);
            avatarView.setBackground(avatarDrawable);
            titleTextView = (TextView) v.findViewById(R.id.title);
            subtitleTextView = (TextView) v.findViewById(R.id.subtitle);
        }

    }

    private final String[] mMonths;

    @NonNull
    private final Map<String, Subject> mSubjectMap;

    public AttendanceAdapter(
            @NonNull ModelFragment<Absence> fragment,
            @NonNull List<Absence> list,
            @NonNull Map<String, Subject> subjectMap) {
        super(fragment, list);
        mSubjectMap = subjectMap;

        mMonths = fragment.getResources().getStringArray(R.array.months);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View v = LayoutInflater.from(context).inflate(R.layout.item_absence, parent, false);
        return new ViewHolder(v, getFragment());
    }

    @Override
    public void onBindViewHolder(ModelFragment.BaseHolder<Absence> holder, int position) {
        super.onBindViewHolder(holder, position);
        ViewHolder h = (ViewHolder) holder;
        Absence absence = getItem(position);

        Subject subject = mSubjectMap.get(absence.subject);
        h.titleTextView.setText(subject == null
                ? UiHelper.TEXT_PLACEHOLDER
                : subject.name);
        h.avatarDrawable.setColor(subject == null ? Palette.GREY : subject.color);
        h.avatarDrawable.invalidateSelf();

        int day = DateUtilz.getDay(absence.date);
        int month = DateUtilz.getMonth(absence.date);
        TextView dt = h.subtitleTextView;
        if (absence.time != 0) {
            String time = DateUtilz.formatLessonTime(absence.time);
            dt.setText(mMonths[month] + " " + day + ", " + time);
        } else dt.setText(mMonths[month] + " " + day);
    }

}
