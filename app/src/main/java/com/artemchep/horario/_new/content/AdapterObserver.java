package com.artemchep.horario._new.content;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.artemchep.horario.aggregator.Aggregator;
import com.artemchep.horario.models.Model;

/**
 * @author Artem Chepurnoy
 */
public class AdapterObserver<T extends Model> implements Aggregator.Observer<T> {

    private RecyclerView.Adapter mAdapter;

    public void setAdapter(@NonNull RecyclerView.Adapter adapter) {
        mAdapter = adapter;
    }

    @Override
    public void add(@NonNull T model, int i) {
        mAdapter.notifyItemInserted(i);
    }

    @Override
    public void set(@NonNull T model, int i) {
        mAdapter.notifyItemChanged(i);
    }

    @Override
    public void remove(@NonNull T model, int i) {
        mAdapter.notifyItemRemoved(i);
    }

    @Override
    public void move(@NonNull T model, int from, int to) {
        mAdapter.notifyItemMoved(from, to);
    }

    @Override
    public void avalanche() {
        mAdapter.notifyDataSetChanged();
    }

}
