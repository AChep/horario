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
package com.artemchep.horario.aggregator;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.artemchep.basic.interfaces.IObservable;
import com.artemchep.horario.models.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Artem Chepurnoy
 */
public class Filter<T extends Model> implements IObservable<Filter.OnFilterChangedListener> {

    public static Filter sEmptyFilter = new Filter<Model>() {

        @Override
        public void add(@NonNull Validator validator) {
            throw new RuntimeException();
        }

        @Override
        public void remove(@NonNull Validator validator) {
            throw new RuntimeException();
        }

        @Nullable
        @Override
        public Parcelable onSaveInstanceState() {
            return null;
        }

        @Override
        public boolean isValid(@NonNull Model model) {
            return true;
        }

    };

    private static final String BUNDLE_VALIDATORS = "validators";

    private ArrayList<Validator> mValidators = new ArrayList<>();
    private ArrayList<OnFilterChangedListener> mListeners = new ArrayList<>();

    /**
     * @author Artem Chepurnoy
     */
    public interface OnFilterChangedListener {

        /**
         * Called when filter changed and the data may require
         * re-arranging.
         */
        void onFilterChanged(@NonNull Filter filter);

    }

    @NonNull
    public List<Validator> getValidators() {
        return mValidators;
    }

    /**
     * @see #remove(Validator)
     * @see #notifyFilterChanged()
     */
    public void add(@NonNull Validator validator) {
        mValidators.add(validator);
    }

    /**
     * @see #add(Validator)
     * @see #notifyFilterChanged()
     */
    public void remove(@NonNull Validator validator) {
        mValidators.remove(validator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerListener(@NonNull OnFilterChangedListener listener) {
        mListeners.add(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unregisterListener(@NonNull OnFilterChangedListener listener) {
        mListeners.remove(listener);
    }

    public void notifyFilterChanged() {
        for (OnFilterChangedListener l : mListeners) {
            l.onFilterChanged(this);
        }
    }

    /**
     * @param p instance state to restore validators from
     * @see #onSaveInstanceState()
     */
    public void onRestoreInstanceState(@Nullable Parcelable p) {
        if (p == null) {
            return;
        }

        mValidators.clear();
        Bundle bundle = (Bundle) p;
        List<Parcelable> list = bundle.getParcelableArrayList(BUNDLE_VALIDATORS);

        if (list != null) for (Parcelable v : list) {
            mValidators.add((Validator) v);
        }
    }

    /**
     * <p>Called when the Filter should save its state. This is a good time to save your
     * validators.</p>
     *
     * @return Necessary information for Filter to be able to restore its state
     * @see #onRestoreInstanceState(Parcelable)
     */
    @Nullable
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(BUNDLE_VALIDATORS, mValidators);
        return bundle;
    }

    /**
     * @return {@code true} if this model passes all of the
     * {@link #mValidators validators}, {@code false} otherwise.
     */
    public boolean isValid(@NonNull T model) {
        for (Validator validator : mValidators) {
            if (!validator.isValid(model)) {
                return false;
            }
        }
        return true;
    }

}
