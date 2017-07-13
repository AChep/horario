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
import android.support.annotation.Nullable;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.artemchep.horario.R;
import com.artemchep.horario.database.models.SubjectGroup;
import com.artemchep.horario.ui.fragments.master.ModelFragment;
import com.artemchep.horario.ui.widgets.GroupUserView;

import java.util.List;

/**
 * @author Artem Chepurnoy
 */
// TODO: Split cards as three separate items: header, user and footer.
public class SubjectsGroupsAdapter extends ModelFragment.BaseAdapter<SubjectGroup> {

    /**
     * @author Artem Chepurnoy
     */
    public enum ActionButton {
        EDIT,
        DELETE,
        TIMETABLE,
    }

    /**
     * @author Artem Chepurnoy
     */
    public interface OnActionButtonClickListener {

        void onClick(@Nullable View view, @NonNull SubjectGroup group, @NonNull ActionButton button);

    }

    /**
     * @author Artem Chepurnoy
     */
    private static class ViewHolder extends ModelFragment.BaseHolder<SubjectGroup> {

        final TextView titleTextView;
        final TextView subtitleTextView;
        final ViewGroup containerView;
        final ImageButton moreButton;
        final ImageButton timetableButton;

        private final SubjectsGroupsAdapter mAdapter;

        ViewHolder(@NonNull View v, @NonNull ModelFragment<SubjectGroup> fragment,
                   @NonNull SubjectsGroupsAdapter adapter) {
            super(v, fragment);
            mAdapter = adapter;

            titleTextView = (TextView) v.findViewById(R.id.title);
            subtitleTextView = (TextView) v.findViewById(R.id.subtitle);
            containerView = (ViewGroup) v.findViewById(R.id.container);
            moreButton = (ImageButton) v.findViewById(R.id.more);
            moreButton.setOnClickListener(this);
            timetableButton = (ImageButton) v.findViewById(R.id.timetable);
            timetableButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.timetable:
                    if (mAdapter.mActionButtonListener != null) {
                        final SubjectGroup data = getItem();
                        assert data != null;
                        mAdapter.mActionButtonListener.onClick(v, data, ActionButton.TIMETABLE);
                    }
                    break;
                case R.id.more: {
                    // Form popup menu and show it
                    Resources res = v.getResources();
                    String[] labels = new String[]{
                            res.getString(R.string.action_edit),
                            res.getString(R.string.action_delete),
                    };
                    PopupMenu popup = new PopupMenu(v.getContext(), itemView.findViewById(R.id.anchor));
                    int i = 1;
                    for (String d : labels) popup.getMenu().add(0, i++, 0, d);
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            if (mAdapter.mActionButtonListener != null) {
                                final SubjectGroup data = getItem();
                                assert data != null;
                                switch (item.getItemId()) {
                                    case 1: // edit
                                        mAdapter.mActionButtonListener.onClick(null, data, ActionButton.EDIT);
                                        break;
                                    case 2: // delete
                                        mAdapter.mActionButtonListener.onClick(null, data, ActionButton.DELETE);
                                        break;
                                    default:
                                        return false;
                                }
                                return true;
                            } else return false;
                        }
                    });
                    popup.show();
                    break;
                }
                default:
                    super.onClick(v);
            }
        }

    }

    @Nullable
    private OnActionButtonClickListener mActionButtonListener;

    public SubjectsGroupsAdapter(
            @NonNull ModelFragment<SubjectGroup> fragment,
            @NonNull List<SubjectGroup> list) {
        super(fragment, list);
    }

    public void setOnActionButtonClickListener(OnActionButtonClickListener listener) {
        mActionButtonListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View v = LayoutInflater.from(context).inflate(R.layout.item_group, parent, false);
        return new ViewHolder(v, getFragment(), this);
    }

    @Override
    public void onBindViewHolder(ModelFragment.BaseHolder<SubjectGroup> holder, int position) {
        super.onBindViewHolder(holder, position);
        ViewHolder h = (ViewHolder) holder;
        SubjectGroup group = getItem(position);
        Resources res = h.itemView.getResources();
        int count = group.users == null ? 0 : group.users.size();

        h.titleTextView.setText(group.name);
        h.subtitleTextView.setText(res.getQuantityString(R.plurals.subject_group_students_count, count, count));

        if (count == 0) {
            h.containerView.removeAllViews();
        } else {
            int i = 0;
            for (int j = h.containerView.getChildCount() - 1; j >= count; j--) {
                h.containerView.removeViewAt(j);
            }
            for (; i < count && i < h.containerView.getChildCount(); i++) {
                View view = h.containerView.getChildAt(i);
                GroupUserView userView = (GroupUserView) view;
                userView.setUser(group.users.get(i));
            }
            for (; i < count; i++) {
                LayoutInflater inflater = LayoutInflater.from(h.containerView.getContext());
                View view = inflater.inflate(R.layout.item_group_user, h.containerView, false);
                GroupUserView userView = (GroupUserView) view;
                userView.setUser(group.users.get(i));
                userView.setBackground(null);
                h.containerView.addView(view);
            }
        }
    }

}
