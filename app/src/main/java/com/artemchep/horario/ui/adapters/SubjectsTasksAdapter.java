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
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.artemchep.horario.Palette;
import com.artemchep.horario.R;
import com.artemchep.horario.models.SubjectTask;
import com.artemchep.horario.ui.fragments.master.ModelFragment;

import java.util.List;

/**
 * @author Artem Chepurnoy
 */
public class SubjectsTasksAdapter extends ModelFragment.BaseAdapter<SubjectTask> {

    /**
     * @author Artem Chepurnoy
     */
    private static class ViewHolder extends ModelFragment.BaseHolder<SubjectTask> {

        final TextView authorTextView;
        final TextView statusTextView;
        final TextView titleTextView;
        final TextView decrTextView;
        final TextView commentsTextView;
        final ImageView iconView;

        final ColorDrawable colorDrawable;

        ViewHolder(@NonNull View v, @NonNull ModelFragment<SubjectTask> fragment) {
            super(v, fragment);
            colorDrawable = new ColorDrawable();

            authorTextView = (TextView) v.findViewById(R.id.author);
            statusTextView = (TextView) v.findViewById(R.id.status);
            titleTextView = (TextView) v.findViewById(R.id.title);
            decrTextView = (TextView) v.findViewById(R.id.description);
            commentsTextView = (TextView) v.findViewById(R.id.comments);
            iconView = (ImageView) v.findViewById(R.id.icon);
            iconView.setBackground(colorDrawable);
        }

    }

    public SubjectsTasksAdapter(
            @NonNull ModelFragment<SubjectTask> fragment,
            @NonNull List<SubjectTask> list) {
        super(fragment, list);
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

        h.authorTextView.setText("Artem Chepurnoy");
        h.statusTextView.setText("21 April, 10:24");
        h.titleTextView.setText(subject.title);

        if (!TextUtils.isEmpty(subject.description)) {
            h.decrTextView.setText(subject.description);
            h.decrTextView.setVisibility(View.VISIBLE);
        } else h.decrTextView.setVisibility(View.GONE);

        int accentColor;
        switch (subject.priority) {
            case SubjectTask.PRIORITY_LOW:
                accentColor = Palette.GREY;
                break;
            case SubjectTask.PRIORITY_HIGH:
                accentColor = Palette.RED;
                break;
            default:
                accentColor = Palette.BLUE;
                break;
        }

        h.colorDrawable.setColor(accentColor);

        switch (subject.type) {
            case SubjectTask.TYPE_ANNOUNCEMENT:
                h.iconView.setImageResource(R.drawable.ic_comment_alert_outline_white_24dp);
                break;
            case SubjectTask.TYPE_ASSIGNMENT:
                h.iconView.setImageResource(R.drawable.ic_comment_text_outline_white_24dp);
                break;
            case SubjectTask.TYPE_QUESTION:
                h.iconView.setImageResource(R.drawable.ic_comment_question_outline_white_24dp);
                break;
            default:
                h.iconView.setImageDrawable(null);
        }

    }

}
