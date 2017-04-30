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

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.artemchep.horario.R;
import com.artemchep.horario.interfaces.IProfile;
import com.artemchep.horario.ui.widgets.AccountHeaderView;

import java.util.List;

/**
 * @author Artem Chepurnoy
 */
public class AccountHeaderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int[] CHECKED_STATE_SET = {android.R.attr.state_activated};
    private static final int[] EMPTY_STATE_SET = {};

    private static final int VIEW_TYPE_PROFILE = 0;
    private static final int VIEW_TYPE_HEADER = 1;
    private static final int VIEW_TYPE_SPACE = 2;

    @NonNull
    private final List<IProfile> mList;

    @NonNull
    private final AccountHeaderView mHeaderView;

    /**
     * @author Artem Chepurnoy
     */
    private static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final ImageView iconImageView;
        final TextView titleTextView;

        private final AccountHeaderAdapter mAdapter;

        private ViewHolder(View itemView, AccountHeaderAdapter adapter) {
            super(itemView);
            mAdapter = adapter;

            itemView.setOnClickListener(this);
            iconImageView = (ImageView) itemView.findViewById(R.id.icon);
            titleTextView = (TextView) itemView.findViewById(R.id.title);
        }

        @Override
        public void onClick(View v) {
            IProfile profile = getItem();
            if (profile != null) {
                mAdapter.mHeaderView.notifyProfileClicked(profile);
            }
        }

        /**
         * @return the item associated with this holder,
         * {@code null} if no one.
         */
        @Nullable
        private IProfile getItem() {
            int i = getAdapterPosition();
            return i != RecyclerView.NO_POSITION
                    ? mAdapter.mList.get(i - 2)
                    : null;
        }

    }

    /**
     * @author Artem Chepurnoy
     */
    private static class EmptyViewHolder extends RecyclerView.ViewHolder {

        private EmptyViewHolder(View itemView) {
            super(itemView);
        }

    }

    public AccountHeaderAdapter(@NonNull List<IProfile> list, @NonNull AccountHeaderView header) {
        mHeaderView = header;
        mList = list;
    }

    private StateListDrawable createDefaultBackground() {
        TypedValue value = new TypedValue();
        if (mHeaderView.getContext().getTheme().resolveAttribute(
                android.support.v7.appcompat.R.attr.colorControlHighlight, value, true)) {
            StateListDrawable drawable = new StateListDrawable();
            drawable.addState(CHECKED_STATE_SET, new ColorDrawable(value.data));
            drawable.addState(EMPTY_STATE_SET, new ColorDrawable(Color.TRANSPARENT));
            return drawable;
        }
        return null;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case VIEW_TYPE_PROFILE: {
                View v = inflater.inflate(R.layout.item_profile, parent, false);
                v.setBackground(createDefaultBackground());
                return new ViewHolder(v, this);
            }
            case VIEW_TYPE_SPACE: {
                View v = inflater.inflate(R.layout.item_profile_space, parent, false);
                return new EmptyViewHolder(v);
            }
            case VIEW_TYPE_HEADER:
                return new EmptyViewHolder(mHeaderView);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) != VIEW_TYPE_PROFILE) {
            return; // no modification needed
        }

        IProfile profile = mList.get(position - 2);
        ViewHolder h = (ViewHolder) holder;
        h.iconImageView.setImageResource(profile.getIcon());
        h.titleTextView.setText(profile.getName());
        h.itemView.setActivated(profile.isSelectable() && profile.getId() == mHeaderView.getCurrentProfile());
    }

    @Override
    public int getItemCount() {
        return mList.size() + 2; // header and space
    }

    @Override
    public int getItemViewType(int position) {
        switch (position) {
            case 0:
                return VIEW_TYPE_HEADER;
            case 1:
                return VIEW_TYPE_SPACE;
            default:
                return VIEW_TYPE_PROFILE;
        }
    }

    @NonNull
    public List<IProfile> getList() {
        return mList;
    }

}
