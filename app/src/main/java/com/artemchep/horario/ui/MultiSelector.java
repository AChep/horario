/*
 * Copyright (C) 2017 Artem Chepurnoy <artemchep@gmail.com>
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
package com.artemchep.horario.ui;

import android.support.annotation.NonNull;
import android.view.View;

import com.artemchep.horario.interfaces.IObservable;

import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * @author Artem Chepurnoy
 */
public class MultiSelector<T> implements IObservable<MultiSelector.Callback<T>> {

    private ArrayList<T> mList = new ArrayList<>();
    private ArrayList<Callback<T>> mListeners = new ArrayList<>();
    private WeakHashMap<View, T> mMap = new WeakHashMap<>();

    private boolean mIsEmpty = true;

    /**
     * @author Artem Chepurnoy
     */
    public interface Callback<T> {

        void onSelectorStatusChanged(@NonNull MultiSelector<T> selector, boolean isEmpty);

        void onSelectorSelectionsChanged(@NonNull MultiSelector<T> selector);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerListener(@NonNull Callback<T> listener) {
        mListeners.add(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unregisterListener(@NonNull Callback<T> listener) {
        mListeners.remove(listener);
    }

    /**
     * Please note that changing this array actually changes
     * current selections. If you want to do that, then make sure you
     * call {@link #bind(Object)} or {@link #bindAll()}.
     */
    @NonNull
    public ArrayList<T> getSelections() {
        return mList;
    }

    public void clear() {
        mIsEmpty = isEmpty();
        mList.clear();
        bindAll();

        updateStatus();
        notifySelectorSelectionsChanged();
    }

    /**
     * Removes selection from selected item and
     * selects not selected item.
     *
     * @see #add(Object)
     * @see #remove(Object)
     */
    public void toggle(@NonNull T data) {
        mIsEmpty = isEmpty();
        //noinspection StatementWithEmptyBody
        if (mList.remove(data)) {
        } else mList.add(data);
        bind(data);

        updateStatus();
        notifySelectorSelectionsChanged();
    }

    /**
     * Adds the selection to item if not selected.
     *
     * @see #remove(Object)
     * @see #toggle(Object)
     */
    public void add(@NonNull T data) {
        //noinspection StatementWithEmptyBody
        if (contains(data)) {
        } else {
            mIsEmpty = isEmpty();
            mList.add(data);
            bind(data);

            updateStatus();
            notifySelectorSelectionsChanged();
        }
    }

    /**
     * Removes the selection from item if selected.
     *
     * @see #add(Object)
     * @see #toggle(Object)
     */
    public void remove(@NonNull T data) {
        mIsEmpty = isEmpty();
        if (mList.remove(data)) {
            bind(data);

            updateStatus();
            notifySelectorSelectionsChanged();
        }
    }

    public boolean contains(@NonNull T data) {
        return mList.contains(data);
    }

    /**
     * @return {@code true} if no one item is selected,
     * {@code false} otherwise.
     */
    public boolean isEmpty() {
        return mList.isEmpty();
    }

    // --------------------------
    // -- EVENTS ----------------
    // --------------------------

    private void notifySelectorStatusChanged() {
        for (Callback<T> listener : mListeners) {
            listener.onSelectorStatusChanged(this, mIsEmpty);
        }
    }

    private void notifySelectorSelectionsChanged() {
        for (Callback<T> listener : mListeners) {
            listener.onSelectorSelectionsChanged(this);
        }
    }

    private void updateStatus() {
        boolean empty = isEmpty();
        if (mIsEmpty != empty) {
            mIsEmpty = empty;
            notifySelectorStatusChanged();
        }
    }

    // --------------------------
    // -- VIEWS -----------------
    // --------------------------

    public void disconnect(@NonNull View itemView) {
        mMap.remove(itemView);
    }

    public void connect(@NonNull View itemView, T key) {
        mMap.put(itemView, key);
    }

    public void bind(T key) {
        for (Map.Entry<View, T> entry : mMap.entrySet()) {
            if (entry.getValue().equals(key)) {
                bind(entry.getKey(), key);
                // It's possible that we have two+ views
                // for one key.
            }
        }
    }

    public void bind(@NonNull View itemView, T key) {
        boolean selected = mList.contains(key);
        itemView.setActivated(selected);
    }

    public void bindAll() {
        for (Map.Entry<View, T> entry : mMap.entrySet()) {
            bind(entry.getKey(), entry.getValue());
        }
    }

}
