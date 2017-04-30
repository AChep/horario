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
import android.support.annotation.Nullable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.artemchep.horario.R;
import com.artemchep.horario.billing.SkuUi;

import java.util.List;

/**
 * @author Artem Chepurnoy
 */
public class SupportMonetaryAdapter extends RecyclerView.Adapter<SupportMonetaryAdapter.ViewHolder> {

    @NonNull
    private final List<SkuUi> mList;

    @Nullable
    private final OnSkuClickListener mListener;

    /**
     * @author Artem Chepurnoy
     */
    public interface OnSkuClickListener {

        void onSkuClick(@NonNull View view, @NonNull SkuUi skuUi);

    }

    /**
     * @author Artem Chepurnoy
     */
    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final ImageView iconImageView;
        final TextView titleTextView;
        final TextView summaryTextView;
        final Button buttonView;

        private final SupportMonetaryAdapter mAdapter;

        ViewHolder(View itemView, SupportMonetaryAdapter adapter) {
            super(itemView);
            mAdapter = adapter;

            iconImageView = (ImageView) itemView.findViewById(R.id.icon);
            titleTextView = (TextView) itemView.findViewById(R.id.title);
            summaryTextView = (TextView) itemView.findViewById(R.id.summary);
            buttonView = (Button) itemView.findViewById(R.id.button);
            buttonView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            SkuUi skuUi = getItem();
            if (mAdapter.mListener != null && skuUi != null) {
                mAdapter.mListener.onSkuClick(v, skuUi);
            }
        }

        /**
         * @return the item associated with this holder,
         * {@code null} if no one.
         */
        @Nullable
        private SkuUi getItem() {
            int i = getAdapterPosition();
            return i != RecyclerView.NO_POSITION
                    ? mAdapter.mList.get(i)
                    : null;
        }

    }

    public SupportMonetaryAdapter(@NonNull List<SkuUi> list, @Nullable OnSkuClickListener listener) {
        mListener = listener;
        mList = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.item_donate, parent, false);
        return new ViewHolder(v, this);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Context context = holder.iconImageView.getContext();
        SkuUi skuUi = getItem(position);

        holder.titleTextView.setText(skuUi.getTitle());
        holder.summaryTextView.setText(skuUi.sku.description);
        // Update button
        holder.buttonView.setEnabled(!skuUi.isPurchased());
        if (skuUi.isPurchased()) {
            holder.buttonView.setText(context.getString(R.string.main_support_donation_purchased));
        } else holder.buttonView.setText(context.getString(R.string.main_support_donation_price,
                skuUi.getPriceCurrency(),
                skuUi.getPriceAmount()));
        // Update icon
        VectorDrawableCompat drawable = VectorDrawableCompat.create(
                context.getResources(), skuUi.icon, null);
        holder.iconImageView.setImageDrawable(drawable);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @NonNull
    public List<SkuUi> getList() {
        return mList;
    }

    @NonNull
    public SkuUi getItem(int position) {
        return mList.get(position);
    }

}
