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
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.artemchep.horario.R;
import com.artemchep.horario.models.Teacher;
import com.artemchep.horario.ui.drawables.CircleDrawable;
import com.artemchep.horario.ui.fragments.master.ModelFragment;
import com.thebluealliance.spectrum.internal.ColorUtil;

import java.util.List;

/**
 * @author Artem Chepurnoy
 */
public class TeachersAdapter extends ModelFragment.BaseAdapter<Teacher> {

    /**
     * @author Artem Chepurnoy
     */
    private static class ViewHolder extends ModelFragment.BaseHolder<Teacher> {

        final TextView avatarView;
        final CircleDrawable avatarDrawable;
        final TextView nameTextView;


        ViewHolder(@NonNull View v, @NonNull ModelFragment<Teacher> fragment) {
            super(v, fragment);
            avatarDrawable = new CircleDrawable();
            avatarView = (TextView) v.findViewById(R.id.color);
            avatarView.setBackground(avatarDrawable);
            nameTextView = (TextView) v.findViewById(R.id.name);
        }

    }

    public TeachersAdapter(
            @NonNull ModelFragment<Teacher> fragment,
            @NonNull List<Teacher> list) {
        super(fragment, list);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View v = LayoutInflater.from(context).inflate(R.layout.item_teacher, parent, false);
        return new ViewHolder(v, getFragment());
    }

    @Override
    public void onBindViewHolder(ModelFragment.BaseHolder<Teacher> holder, int position) {
        super.onBindViewHolder(holder, position);
        ViewHolder h = (ViewHolder) holder;
        Teacher teacher = getItem(position);

        int color = teacher.color;
        int textColor = ColorUtil.isColorDark(color) ? Color.WHITE : Color.BLACK;
        h.avatarDrawable.setColor(color);
        h.avatarDrawable.invalidateSelf();
        /*
        if (teacher.name != null && teacher.name.length() > 0) {
            h.avatarView.setText(Character.toString(teacher.name.charAt(0)));
        } else h.avatarView.setText(null);
        */
        h.avatarView.setTextColor(textColor);
        h.nameTextView.setText(teacher.name);
    }

}
