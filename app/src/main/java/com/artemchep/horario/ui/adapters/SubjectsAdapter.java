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

import com.artemchep.horario.R;
import com.artemchep.horario.models.Subject;
import com.artemchep.horario.ui.drawables.CircleDrawable;
import com.artemchep.horario.ui.fragments.master.ModelFragment;

import java.util.List;

/**
 * @author Artem Chepurnoy
 */
public class SubjectsAdapter extends ModelFragment.BaseAdapter<Subject> {

    /**
     * @author Artem Chepurnoy
     */
    private static class ViewHolder extends ModelFragment.BaseHolder<Subject> {

        final TextView nameTextView;
        final TextView abbrTextView;
        final View colorView;

        final CircleDrawable colorDrawable;

        ViewHolder(@NonNull View v, @NonNull ModelFragment<Subject> fragment) {
            super(v, fragment);
            colorDrawable = new CircleDrawable();
            nameTextView = (TextView) v.findViewById(R.id.name);
            abbrTextView = (TextView) v.findViewById(R.id.abbreviation);
            colorView = v.findViewById(R.id.color);
            colorView.setBackground(colorDrawable);
        }

    }

    public SubjectsAdapter(
            @NonNull ModelFragment<Subject> fragment,
            @NonNull List<Subject> list) {
        super(fragment, list);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View v = LayoutInflater.from(context).inflate(R.layout.item_subject, parent, false);
        return new ViewHolder(v, getFragment());
    }

    @Override
    public void onBindViewHolder(ModelFragment.BaseHolder<Subject> holder, int position) {
        super.onBindViewHolder(holder, position);
        ViewHolder h = (ViewHolder) holder;
        Subject subject = getItem(position);

        h.nameTextView.setText(subject.name);
        h.abbrTextView.setText(subject.abbreviation);
        h.colorDrawable.setColor(subject.color);
    }

}
