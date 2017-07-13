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
package com.artemchep.horario.ui.fragments.master;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialcab.MaterialCab;
import com.artemchep.basic.tests.Check;
import com.artemchep.basic.ui.MultiSelector;
import com.artemchep.horario.R;
import com.artemchep.horario.aggregator.Aggregator;
import com.artemchep.horario.aggregator.Filter;
import com.artemchep.horario.database.Persy;
import com.artemchep.horario.models.Model;
import com.artemchep.horario.ui.activities.ActivityHorario;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * @author Artem Chepurnoy
 */
public abstract class ModelFragment<T extends Model> extends MasterFragment implements
        MaterialCab.Callback,
        Filter.OnFilterChangedListener {

    public static final String EXTRA_TIMETABLE_PATH = "extra::timetable_path";
    public static final String EXTRA_EDITABLE = "extra::editable";

    private static final String TAG = "ModelFragment";

    private static final String SAVED_ITEMS = "items";
    private static final String SAVED_POSITION = "position";
    private static final String SAVED_SELECTION = "selection";
    private static final String SAVED_FILTER = "filter";

    private RecyclerView mRecyclerView;
    private BaseAdapter<T> mAdapter;
    private View mEmptyView;

    private Filter<T> mFilter;
    public Aggregator<T> mAggregator;
    @NonNull
    public final Map<String, T> mHiddenMap = new HashMap<>();
    @NonNull
    private final Aggregator.Observer<T> mObserver = new Aggregator.Observer<T>() {

        @Override
        public void add(@NonNull T model, int i) {
            getAdapter().notifyItemInserted(i);
            refreshEmptyView();
        }

        @Override
        public void set(@NonNull T model, int i) {
            getAdapter().notifyItemChanged(i);
        }

        @Override
        public void remove(@NonNull T model, int i) {
            mSelector.remove(model.key);
            getAdapter().notifyItemRemoved(i);
            refreshEmptyView();
        }

        @Override
        public void move(@NonNull T model, int from, int to) {
            getAdapter().notifyItemRemoved(from);
            getAdapter().notifyItemInserted(to);
            refreshEmptyView();
        }

        @Override
        public void avalanche() {
            getAdapter().notifyDataSetChanged();
            refreshEmptyView();
        }

        private void refreshEmptyView() {
            final boolean empty = mAdapter.getItems().isEmpty();
            mEmptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
            mRecyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
        }

    };

    protected boolean mEditable;
    protected String mTimetablePath;
    protected Persy.Watcher<T> mWatcher;
    private boolean mDatabaseChildRegistered;

    @NonNull
    private final MultiSelector<String> mSelector = new MultiSelector<>();
    @NonNull
    private final ChildEventListener mModelEventListener = new ChildEventListener() {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            T model = mWatcher.getModel();
            mAggregator.put(model);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            T model = mWatcher.getModel();
            mAggregator.put(model);
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            T old = mWatcher.getModel();
            mAggregator.remove(old);
            // we must remove it from actual filter after removing
            // it from manager.
            mHiddenMap.remove(old.key);
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            String clazz = getType().getSimpleName();
            Timber.tag(TAG).w("Failed to retrieve " + clazz + "; error_code=" + databaseError);
        }

    };

    @NonNull
    protected abstract Comparator<T> onCreateComparator();

    @NonNull
    protected abstract String getSnackBarRemoveMessage(List<T> list);

    @NonNull
    protected abstract BaseAdapter<T> onCreateAdapter();

    @NonNull
    protected abstract Class<T> getType();

    @NonNull
    protected abstract String getPath();

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        mFilter = onCreateFilter();
        mFilter.registerListener(this);
        mAggregator = new Aggregator<>(new Filter<T>() {

            /**
             * Filters models by existing validators and hides
             * items from {@link ModelFragment#mHiddenMap hidden map}.
             */
            @Override
            public boolean isValid(@NonNull T model) {
                return mFilter.isValid(model) && !mHiddenMap.containsKey(model.key);
            }

        }, onCreateComparator(), mObserver);

        // Get timetable data
        Bundle args = getArguments();
        ActivityHorario activity = (ActivityHorario) getActivity();
        mTimetablePath = args.getString(EXTRA_TIMETABLE_PATH);
        mEditable = args.getBoolean(EXTRA_EDITABLE, true);
        String path = getPath();
        Timber.tag(TAG).d("Using path=" + path);
        mWatcher = activity.getPersy().watchFor(getType(), path);

        // Init selector
        mSelector.registerListener(new MultiSelector.Callback<String>() {
            @Override
            public void onSelectorStatusChanged(
                    @NonNull MultiSelector<String> selector,
                    boolean isEmpty) {
                if (isEmpty) {
                    getMainActivity().finishContextualActionBar();
                } else getMainActivity().startContextualActionBar(ModelFragment.this);
            }

            @Override
            public void onSelectorSelectionsChanged(@NonNull MultiSelector<String> selector) {
                int count = selector.getSelections().size();
                getMainActivity().getContextualActionBar().setTitle("" + count);
            }
        });
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedState) {
        super.onViewCreated(view, savedState);
        mAdapter = onCreateAdapter();
        mEmptyView = view.findViewById(R.id.empty);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedState) {
        super.onViewStateRestored(savedState);

        if (savedState == null) {
            return;
        }

        Parcelable state;
        state = savedState.getParcelable(SAVED_POSITION);
        mRecyclerView.getLayoutManager().onRestoreInstanceState(state);
        state = savedState.getParcelable(SAVED_FILTER);
        mFilter.onRestoreInstanceState(state);

        List<T> list = savedState.getParcelableArrayList(SAVED_ITEMS);
        mAggregator.replaceAll(list);

        List<String> listStr = savedState.getStringArrayList(SAVED_SELECTION);
        mSelector.getSelections().addAll(listStr);
        mSelector.bindAll();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mAdapter.getItems().isEmpty()) {
            mDatabaseChildRegistered = true;
            mWatcher.addListener(mModelEventListener);

            // Set loaded data
            Collection<T> list = mWatcher.getMap().values();
            mAggregator.replaceAll(list);
            return;
        }

        // As we want to save scroll state, load the whole data
        // at once and then track children changes. This way is a lil bit
        // retarded, but seems to be the only one.
        mWatcher.getDatabase().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<T> models = new ArrayList<>();
                for (DataSnapshot s : dataSnapshot.getChildren()) {
                    T data = s.getValue(getType());
                    data.key = s.getKey();
                    models.add(data);
                }
                mAggregator.replaceAll(models);

                // remove old selections
                List<String> list = mSelector.getSelections();
                removing_old_selections:
                for (int i = list.size() - 1; i >= 0; i--) {
                    for (T data : mAdapter.getItems()) {
                        if (data.key.equals(list.get(i))) {
                            continue removing_old_selections;
                        }
                    }
                    mSelector.remove(list.get(i));
                }

                for (String key : mSelector.getSelections()) {
                    Log.d("BOO_WELP", key);
                }

                // NOTE:
                // we assume that between this method, and
                // child events database can not be changed.
                mDatabaseChildRegistered = true;
                mWatcher.addListener(mModelEventListener);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                String clazz = getType().getSimpleName();
                Timber.tag(TAG).w("Failed to retrieve " + clazz + "; " +
                        "[single_value_event] " +
                        "error_code=" + databaseError);
            }
        });
    }

    @Override
    public void onStop() {
        if (mDatabaseChildRegistered) {
            mWatcher.removeListener(mModelEventListener);
            mDatabaseChildRegistered = false;
        }
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mRecyclerView != null) {
            RecyclerView.LayoutManager lm = mRecyclerView.getLayoutManager();
            outState.putParcelable(SAVED_POSITION, lm.onSaveInstanceState());
        }
        outState.putParcelableArrayList(SAVED_ITEMS, mAggregator.getModelsAll());
        outState.putStringArrayList(SAVED_SELECTION, mSelector.getSelections());
        outState.putParcelable(SAVED_FILTER, mFilter.onSaveInstanceState());

        for (String key : mSelector.getSelections()) {
            Log.d("BOO_SAVE", key);
        }
    }

    @Override
    public void onDetach() {
        mFilter.unregisterListener(this);
        super.onDetach();
    }

    /**
     * Called on item click.
     *
     * @param view clicked view
     * @param item data
     */
    @SuppressWarnings("UnusedParameters")
    public void onItemClick(@NonNull View view, @NonNull T item) { /* empty */ }

    @NonNull
    public Filter<T> onCreateFilter() {
        return new Filter<>();
    }

    @Override
    public void onFilterChanged(@NonNull Filter filter) {
        Check.getInstance().isTrue(filter == mFilter);
        mAggregator.refilter();
    }

    @NonNull
    public Filter<T> getFilter() {
        return mFilter;
    }

    @NonNull
    public BaseAdapter<T> getAdapter() {
        return mAdapter;
    }

    @NonNull
    public MultiSelector<String> getSelector() {
        return mSelector;
    }

    /**
     * Builds a list of selected models.
     */
    @NonNull
    public List<T> getSelections() {
        final List<T> list = new ArrayList<>();
        final BaseAdapter<T> adapter = getAdapter();
        for (String key : getSelector().getSelections()) {
            for (int i = 0; i < adapter.getItemCount(); i++) {
                T item = adapter.getItem(i);
                if (item.key.equals(key)) list.add(item);
            }
        }
        return list;
    }

    /**
     * @author Artem Chepurnoy
     */
    public static class BaseHolder<T extends Model>
            extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {

        @NonNull
        private final ModelFragment<T> mFragment;
        @NonNull
        private final MultiSelector<String> mSelector;

        public BaseHolder(@NonNull View v, @NonNull ModelFragment<T> fragment) {
            super(v);
            mFragment = fragment;
            mSelector = fragment.mSelector;
            v.setOnClickListener(this);
            v.setLongClickable(true);
            v.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            final T data = getItem();
            if (data == null) return;

            if (mSelector.isEmpty()) {
                mFragment.onItemClick(v, data);
            } else mSelector.toggle(data.key);
        }

        @Override
        public boolean onLongClick(View v) {
            final T data = getItem();
            if (data == null || mSelector.contains(data.key) || !mFragment.mEditable) return false;

            mSelector.add(data.key);
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
                    ? mFragment.mAdapter.getItem(i)
                    : null;
        }

    }

    /**
     * @author Artem Chepurnoy
     */
    public abstract static class BaseAdapter<T extends Model> extends RecyclerView.Adapter<BaseHolder<T>> {

        @NonNull
        private final ModelFragment<T> mFragment;
        @NonNull
        private final MultiSelector<String> mSelector;
        @NonNull
        private final List<T> mItems;

        public BaseAdapter(@NonNull ModelFragment<T> fragment, @NonNull List<T> items) {
            mFragment = fragment;
            mSelector = mFragment.mSelector;
            mItems = items;
        }

        @Override
        public int getItemCount() {
            return getItems().size();
        }

        @Override
        public void onBindViewHolder(BaseHolder<T> holder, int position) {
            String key = getItem(position).key;
            mSelector.connect(holder.itemView, key);
            mSelector.bind(holder.itemView, key);
        }

        @SuppressWarnings("WeakerAccess")
        @NonNull
        protected T getItem(int position) {
            return getItems().get(position);
        }

        @SuppressWarnings("WeakerAccess")
        @NonNull
        protected List<T> getItems() {
            return mItems;
        }

        @SuppressWarnings("WeakerAccess")
        @NonNull
        protected ModelFragment<T> getFragment() {
            return mFragment;
        }

    }

    // --------------------------
    // -- ACTION MODE -----------
    // --------------------------

    @Override
    public boolean onCabCreated(MaterialCab cab, Menu menu) {
        cab.setMenu(R.menu.master_context);
        return true;
    }

    @Override
    public boolean onCabItemClicked(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_delete:
                // Build items list.
                List<T> list = new ArrayList<>();
                for (String key : mSelector.getSelections()) {
                    for (int i = 0; i < mAdapter.getItemCount(); i++) {
                        final T item = mAdapter.getItem(i);
                        if (item.key.equals(key)) {
                            list.add(item);
                        }
                    }
                }

                removeModels(list);
                break;
            default:
                return false;
        }
        getMainActivity().finishContextualActionBar();
        return true;
    }

    @Override
    public boolean onCabFinished(MaterialCab cab) {
        mSelector.clear();
        return true;
    }

    public void removeModels(final @NonNull List<T> list) {
        View view = getView();
        if (view == null) {
            return;
        }

        // Create snack-bar message
        String message = getSnackBarRemoveMessage(list);

        // Show snack-bar
        View coordinator = getMainActivity().mContainers.findViewById(R.id.coordinator_layout_fab);
        Snackbar.make(coordinator != null ? coordinator : view, message, Snackbar.LENGTH_LONG)
                .addCallback(new Snackbar.Callback() {
                    @Override
                    public void onShown(Snackbar sb) {
                        super.onShown(sb);
                        for (T t : list) mHiddenMap.put(t.key, t);
                        mAggregator.refilter();
                    }

                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        super.onDismissed(transientBottomBar, event);
                        if (event == DISMISS_EVENT_ACTION) {
                            for (T t : list) mHiddenMap.remove(t.key);
                            mAggregator.refilter();
                            return;
                        }

                        Map<String, Object> childUpdates = new HashMap<>();
                        for (T t : list) childUpdates.put(t.key, null);
                        mWatcher.getDatabase().updateChildren(childUpdates);
                    }
                })
                .setAction(R.string.snackbar_undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                })
                .show();
    }

}
