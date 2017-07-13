package com.artemchep.horario._new.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.artemchep.basic.ui.MultiSelector;
import com.artemchep.horario.R;
import com.artemchep.horario._new.adapters.AdapterModel;
import com.artemchep.horario._new.content.AdapterObserver;
import com.artemchep.horario.aggregator.Aggregator;
import com.artemchep.horario.aggregator.Filter;
import com.artemchep.horario.models.Model;

import java.util.Comparator;
import java.util.List;

/**
 * @author Artem Chepurnoy
 */
public abstract class FragmentModel<T extends Model> extends Fragment {

    @NonNull
    private final MultiSelector<String> mSelector = new MultiSelector<>();
    @NonNull
    private final AdapterObserver mObserver = new AdapterObserver<>();
    @NonNull
    private final Aggregator<T> mAggregator = new Aggregator<>(
            onCreateFilter(), onCreateComparator(), mObserver);

    private View mEmptyView;

    private AdapterModel<T, ? extends AdapterModel.ViewHolder<T>> mAdapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mEmptyView = view.findViewById(R.id.empty);
        mAdapter = onCreateAdapter(mAggregator.getModels());
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                // Show or hide empty view
                mEmptyView.setVisibility(mAdapter.getItemCount() > 0 ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                mEmptyView.setVisibility(mAdapter.getItemCount() > 0 ? View.GONE : View.VISIBLE);
            }
        });

        mObserver.setAdapter(mAdapter);
        RecyclerView rv = view.findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setHasFixedSize(false);
        rv.setAdapter(mAdapter);
    }

    public abstract Filter<T> onCreateFilter();

    public abstract Comparator<T> onCreateComparator();

    public abstract AdapterModel<T, ? extends AdapterModel.ViewHolder<T>> onCreateAdapter(List<T> list);

    public AdapterModel<T, ? extends AdapterModel.ViewHolder<T>> getAdapter() {
        return mAdapter;
    }

    @NonNull
    public MultiSelector<String> getSelector() {
        return mSelector;
    }

    @NonNull
    public Aggregator<T> getAggregator() {
        return mAggregator;
    }

    public void onItemClick(View v, T data) {

    }

}
