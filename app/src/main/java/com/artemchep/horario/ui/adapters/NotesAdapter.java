/*
 * Copyright (C) 2016 Artem Chepurnoy <artemchep@gmail.com>
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
package com.artemchep.horario.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.artemchep.horario.R;
import com.artemchep.horario.models.Note;
import com.artemchep.horario.models.Subject;
import com.artemchep.horario.ui.UiHelper;
import com.artemchep.horario.ui.fragments.master.ModelFragment;
import com.artemchep.horario.utils.DateUtilz;
import com.artemchep.horario.utils.ViewUtils;
import com.thebluealliance.spectrum.internal.ColorUtil;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * @author Artem Chepurnoy
 */
public class NotesAdapter extends ModelFragment.BaseAdapter<Note> {

    /**
     * @author Artem Chepurnoy
     */
    private static class ViewHolder extends ModelFragment.BaseHolder<Note> {

        final TextView titleTextView;
        final TextView contentTextView;
        final TextView subjectTextView;
        final TextView dueTextView;

        ViewHolder(@NonNull View v, @NonNull ModelFragment<Note> fragment) {
            super(v, fragment);
            titleTextView = (TextView) v.findViewById(R.id.title);
            contentTextView = (TextView) v.findViewById(R.id.content);
            subjectTextView = (TextView) v.findViewById(R.id.subjects);
            dueTextView = (TextView) v.findViewById(R.id.due);
        }

    }

    private static final char NON_BREAKING_SPACE = ' ';

    @NonNull
    private final Map<String, Subject> mMap;
    @NonNull
    private final String[] mMonths;

    private final int mNow;

    public NotesAdapter(
            @NonNull ModelFragment<Note> fragment, @NonNull List<Note> list,
            @NonNull Map<String, Subject> map) {
        super(fragment, list);
        mMap = map;
        mMonths = fragment.getResources().getStringArray(R.array.months);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        mNow = DateUtilz.mergeDate(year, month, day);
    }

    @Override
    public ModelFragment.BaseHolder<Note> onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View v = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false);
        return new ViewHolder(v, getFragment());
    }

    @Override
    public void onBindViewHolder(ModelFragment.BaseHolder<Note> holder, int position) {
        super.onBindViewHolder(holder, position);
        ViewHolder h = (ViewHolder) holder;
        Note note = getItem(position);

        ViewUtils.setTextViewText(h.titleTextView, note.title);
        ViewUtils.setTextViewText(h.contentTextView, note.content);
        if (note.subjects == null || note.subjects.isEmpty()) {
            h.subjectTextView.setVisibility(View.GONE);
        } else {
            // TODO: Cache built subjects string
            // it will speed up scroll for a little.
            int start = 0;
            SpannableStringBuilder sb = new SpannableStringBuilder();
            for (String key : note.subjects) {
                int colorBg = 0x33aaaaaa;
                int colorText = 0x33aaaaaa;
                String title;

                // Load subject info
                Subject subject = mMap.get(key);
                if (subject != null) {
                    title = subject.name.replace(" ", NON_BREAKING_SPACE + "");
                    colorBg = subject.color;
                    colorText = ColorUtil.isColorDark(colorBg) ? Color.WHITE : Color.BLACK;
                } else title = UiHelper.getPlaceholderText(key);

                // Colorize text
                sb.append(NON_BREAKING_SPACE).append(title).append(NON_BREAKING_SPACE).append(' ');
                sb.setSpan(new BackgroundColorSpan(colorBg), start, sb.length() - 1, 0);
                sb.setSpan(new ForegroundColorSpan(colorText), start, sb.length() - 1, 0);
                start = sb.length();
            }
            h.subjectTextView.setText(sb);
            h.subjectTextView.setVisibility(View.VISIBLE);
        }

        if (note.due > 0) {
            int day = DateUtilz.getDay(note.due);
            int month = DateUtilz.getMonth(note.due);
            h.dueTextView.setText(mMonths[month] + " " + day);
            h.dueTextView.setVisibility(View.VISIBLE);
            h.dueTextView.setAlpha(note.due >= mNow ? 1f : 0.3f);
        } else h.dueTextView.setVisibility(View.GONE);

    }

}
