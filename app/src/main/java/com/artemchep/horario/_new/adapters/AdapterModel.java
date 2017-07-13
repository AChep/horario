package com.artemchep.horario._new.adapters;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.artemchep.horario._new.fragments.FragmentModel;
import com.artemchep.horario.models.Model;

import java.util.List;

import timber.log.Timber;

/**
 * Base adapter for use with {@link FragmentModel}. Supports multi-selection and
 * on item click listener from the box.
 *
 * @author Artem Chepurnoy
 */
public abstract class AdapterModel<T extends Model, H extends AdapterModel.ViewHolder<T>> extends RecyclerView.Adapter<H> {

    @NonNull
    protected final List<T> mList;
    @NonNull
    protected final FragmentModel<T> mFragment;

    /**
     * @author Artem Chepurnoy
     */
    public static class ViewHolder<T extends Model> extends RecyclerView.ViewHolder implements
            View.OnClickListener,
            View.OnLongClickListener {

        protected FragmentModel<T> mFragment;

        public ViewHolder(View itemView, FragmentModel<T> fragment) {
            super(itemView);
            mFragment = fragment;
            itemView.setOnClickListener(this);
            itemView.setLongClickable(true);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            final T data = getItem();
            if (data == null) {
                Timber.wtf("Clicked data-less item");
                return;
            }

            if (mFragment.getSelector().isEmpty()) {
                mFragment.onItemClick(v, data);
            } else mFragment.getSelector().toggle(data.key);
        }

        @Override
        public boolean onLongClick(View v) {
            final T data = getItem();
            if (data == null) {
                Timber.wtf("Clicked data-less item");
                return false;
            }

            if (mFragment.getSelector().contains(data.key)) return false;
            mFragment.getSelector().add(data.key);
            return true;
        }

        /**
         * @return the item associated with this holder,
         * {@code null} if no one.
         */
        @Nullable
        protected T getItem() {
            int i = getAdapterPosition();
            return i != RecyclerView.NO_POSITION
                    ? mFragment.getAdapter().getItem(i)
                    : null;
        }

    }

    public AdapterModel(@NonNull FragmentModel<T> fragment, @NonNull List<T> list) {
        mList = list;
        mFragment = fragment;
    }

    @Override
    public void onBindViewHolder(H holder, int position) {
        String key = getItem(position).key;
        mFragment.getSelector().connect(holder.itemView, key);
        mFragment.getSelector().bind(holder.itemView, key);
    }

    @NonNull
    public final T getItem(int position) {
        return mList.get(position);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

}
