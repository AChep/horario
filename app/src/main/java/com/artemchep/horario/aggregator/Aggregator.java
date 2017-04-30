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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.artemchep.basic.interfaces.IObservable;
import com.artemchep.basic.tests.Check;
import com.artemchep.horario.models.Model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * @author Artem Chepurnoy
 */
public class Aggregator<T extends Model> implements IObservable<Aggregator.Observer<T>> {

    private boolean mObserverEnabled = true;

    @NonNull
    private final Filter<T> mFilter;
    @NonNull
    private final Comparator<T> mComparator;
    @NonNull
    private final Observer<T> mObserver = new Observer<T>() {

        @Override
        public void add(@NonNull T model, int i) {
            if (mObserverEnabled) {
                for (Observer<T> observer : mListeners) {
                    observer.add(model, i);
                }
            }
        }

        @Override
        public void set(@NonNull T model, int i) {
            if (mObserverEnabled) {
                for (Observer<T> observer : mListeners) {
                    observer.set(model, i);
                }
            }
        }

        @Override
        public void remove(@NonNull T model, int i) {
            if (mObserverEnabled) {
                for (Observer<T> observer : mListeners) {
                    observer.remove(model, i);
                }
            }
        }

        @Override
        public void move(@NonNull T model, int from, int to) {
            if (mObserverEnabled) {
                for (Observer<T> observer : mListeners) {
                    observer.move(model, from, to);
                }
            }
        }

        @Override
        public void avalanche() {
            if (mObserverEnabled) {
                for (Observer<T> observer : mListeners) {
                    observer.avalanche();
                }
            }
        }
    };

    @NonNull
    private final ArrayList<T> mList;
    @NonNull
    private final ArrayList<T> mObject;
    @NonNull
    private final ArrayList<Observer<T>> mListeners;

    private Observer<T> mObserverAll;

    @Override
    public void registerListener(@NonNull Observer<T> listener) {
        mListeners.add(listener);
    }

    @Override
    public void unregisterListener(@NonNull Observer<T> listener) {
        mListeners.remove(listener);
    }

    /**
     * @author Artem Chepurnoy
     */
    public interface Observer<T extends Model> {

        void add(@NonNull T model, int i);

        void set(@NonNull T model, int i);

        void remove(@NonNull T model, int i);

        void move(@NonNull T model, int from, int to);

        void avalanche();

    }

    public Aggregator(
            @NonNull Filter<T> filter,
            @NonNull Comparator<T> comparator,
            @NonNull Observer<T> observer) {
        mFilter = filter;
        mComparator = comparator;
        mList = new ArrayList<>();
        mObject = new ArrayList<>();
        mListeners = new ArrayList<>();
        mListeners.add(observer);
    }

    public void setModelsAllObserver(@Nullable Observer<T> observer) {
        // just to be sure developer knows
        // what he is doing.
        Check.getInstance().isTrue(mObserverAll == null);
        mObserverAll = observer;
    }

    public void put(T model) {
        int i = indexOf(model);
        if (i >= 0) {
            // replace old model with a new one
            // in global list
            T old = mList.get(i);
            if (old.equals(model)) {
                // updating list won't change anything
                return;
            }

            mList.set(i, model);
            if (mObserverAll != null) mObserverAll.set(model, i);

            // update local list
            boolean validNew = mFilter.isValid(model);
            boolean validOld = mFilter.isValid(old);
            if (validNew && validOld) {
                // replace old model with a
                // new one
                int j = indexOf(mObject, old);
                if (j >= 0) {
                    // list may require re-sorting
                    int d = mComparator.compare(model, old);
                    if (d == 0) {
                        // new model has the same position in
                        // list as the old one.
                        mObject.set(j, model);
                        mObserver.set(model, j);
                    } else {
                        int a = 0;
                        int b = mObject.size() - 1;

                        if (d < 0) {
                            if (j == a) {
                                // new model should be before old one, and
                                // the old one is first in list.
                                mObject.set(j, model);
                                mObserver.set(model, j);
                                return;
                            } else b = Math.max(j - 1, a);
                        } else {
                            if (j == b) {
                                // new model should be after old one, and
                                // the old one is last in list.
                                mObject.set(j, model);
                                mObserver.set(model, j);
                                return;
                            } else a = Math.min(j + 1, b);
                        }

                        int x = binarySearch(mObject, model, a, b);
                        if (x > j) {
                            x--;
                        }

                        if (x == j) {
                            mObject.set(j, model);
                            mObserver.set(model, j);
                        } else {
                            mObject.remove(j);
                            mObserver.remove(model, j);

                            mObject.add(x, model);
                            mObserver.add(model, x);

                            // TODO: Use Observer#move(...) instead of remove & add. Note that
                            // none of actual observers support this thing.

                            // This happened to be too complex to support.
                            // mObserver.move(model, j, x);
                        }
                    }
                } else throw new IllegalStateException();
            } else if (validNew) {
                // add a new model to the list
                int x = binarySearch(mObject, model);
                mObject.add(x, model);
                mObserver.add(model, x);
            } else if (validOld) {
                // remove old model from the list
                int j = indexOf(mObject, old);
                if (j >= 0) {
                    mObject.remove(j);
                    mObserver.remove(model, j);
                } else throw new IllegalStateException();
            }
        } else {
            int j = mList.size();
            mList.add(j, model);
            if (mObserverAll != null) mObserverAll.add(model, j);

            // update local list
            boolean validNew = mFilter.isValid(model);
            if (validNew) {
                // add a new model to the local list
                int x = binarySearch(mObject, model);
                mObject.add(x, model);
                mObserver.add(model, x);
            }
        }
    }

    public void remove(T model) {
        int i = indexOf(model);
        if (i >= 0) {
            T old = mList.get(i);
            mList.remove(i);
            if (mObserverAll != null) mObserverAll.remove(model, i);

            // update local list
            boolean validNew = mFilter.isValid(model);
            if (validNew) {
                // remove old model from the list
                int j = indexOf(mObject, old);
                if (j >= 0) {
                    mObject.remove(j);
                    mObserver.remove(model, j);
                } else throw new IllegalStateException();
            }
        }
    }

    public void reset() {
        mObject.clear();
        mList.clear();
    }

    public void replaceAll(Collection<T> list) {
        mObject.clear();
        mList.clear();

        mObserverEnabled = false;
        for (T model : list) {
            put(model);
        }
        mObserverEnabled = true;
        if (mObserverAll != null) mObserverAll.avalanche();
        mObserver.avalanche();
    }

    public void refilter() {
        refilter(false);
    }

    public void refilter(boolean avalanche) {
        if (avalanche) {
            List<T> list = new ArrayList<>(mList);
            replaceAll(list);
            return;
        }

        for (int i = mObject.size() - 1; i >= 0; i--) {
            T old = mObject.get(i);
            if (mFilter.isValid(old)) {
            } else {
                mObject.remove(i);
                mObserver.remove(old, i);
            }
        }

        for (T model : mList) {
            int j = indexOf(mObject, model);
            if (j < 0 && mFilter.isValid(model)) {
                // add a new model to the local list
                int x = binarySearch(mObject, model);
                mObject.add(x, model);
                mObserver.add(model, x);
            }
        }
    }

    public int indexOf(T model) {
        return indexOf(mList, model);
    }

    public int indexOf(List<T> list, T model) {
        final int size = list.size();
        for (int i = 0; i < size; i++) {
            if (TextUtils.equals(list.get(i).key, model.key)) {
                return i;
            }
        }
        return -1;
    }

    private int binarySearch(List<T> list, T model) {
        return list.isEmpty() ? 0 : binarySearch(list, model, 0, list.size() - 1);
    }

    private int binarySearch(List<T> list, T model, int a, int b) {
        if (a == b) {
            T old = list.get(a);
            int d = mComparator.compare(model, old);
            return d > 0 ? a + 1 : a;
        }

        int c = (a + b) / 2;
        T old = list.get(c);
        int d = mComparator.compare(model, old);
        if (d == 0) {
            return c;
        } else if (d < 0) {
            return binarySearch(list, model, a, Math.max(c - 1, 0));
        } else return binarySearch(list, model, Math.min(c + 1, b), b);
    }

    @NonNull
    public final ArrayList<T> getModels() {
        return mObject;
    }

    @NonNull
    public final ArrayList<T> getModelsAll() {
        return mList;
    }

}
