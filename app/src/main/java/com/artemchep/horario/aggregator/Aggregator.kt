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
package com.artemchep.horario.aggregator

import android.text.TextUtils
import com.artemchep.horario.interfaces.IObservable
import com.artemchep.horario.interfaces.Unique
import java.util.*

/**
 * @author Artem Chepurnoy
 */
class Aggregator<T : Unique>(
        private val validator: (T) -> Boolean,
        private val comparator: Comparator<T>) : IObservable<Aggregator.Observer<T>> {

    private var mObserverEnabled = true
    private val mObserver = object : Observer<T> {

        override fun add(model: T, i: Int) {

            if (mObserverEnabled) {
                for (observer in mListeners) {
                    observer.add(model, i)
                }
            }
        }

        override operator fun set(model: T, i: Int) {
            if (mObserverEnabled) {
                for (observer in mListeners) {
                    observer.set(model, i)
                }
            }
        }

        override fun remove(model: T, i: Int) {
            if (mObserverEnabled) {
                for (observer in mListeners) {
                    observer.remove(model, i)
                }
            }
        }

        override fun move(model: T, from: Int, to: Int) {
            if (mObserverEnabled) {
                for (observer in mListeners) {
                    observer.move(model, from, to)
                }
            }
        }

        override fun avalanche() {
            if (mObserverEnabled) {
                for (observer in mListeners) {
                    observer.avalanche()
                }
            }
        }
    }

    val modelsAll: ArrayList<T> = ArrayList()
    val models: ArrayList<T> = ArrayList()
    private val mListeners: ArrayList<Observer<T>> = ArrayList()

    override fun registerListener(listener: Observer<T>) {
        mListeners.add(listener)
    }

    override fun unregisterListener(listener: Observer<T>) {
        mListeners.remove(listener)
    }

    /**
     * @author Artem Chepurnoy
     */
    interface Observer<T : Unique> {

        fun add(model: T, i: Int)

        fun set(model: T, i: Int)

        fun remove(model: T, i: Int)

        fun move(model: T, from: Int, to: Int)

        fun avalanche()

    }

    fun put(model: T): T? {
        val i = indexOf(model)
        if (i >= 0) {
            // replace old model with a new one
            // in global list
            val old = modelsAll[i]
            if (old === model) {
                // updating list won't change anything
                return old
            }

            modelsAll[i] = model

            // update local list
            val validNew = validator(model)
            val validOld = validator(old)
            if (validNew && validOld) {
                // replace old model with a
                // new one
                val j = indexOf(models, old)
                if (j >= 0) {
                    // list may require re-sorting
                    val d = comparator.compare(model, old)
                    if (d == 0) {
                        // new model has the same position in
                        // list as the old one.
                        models[j] = model
                        mObserver[model] = j
                    } else {
                        var a = 0
                        var b = models.size - 1

                        if (d < 0) {
                            if (j == a) {
                                // new model should be before old one, and
                                // the old one is first in list.
                                models[j] = model
                                mObserver[model] = j
                                return old
                            } else
                                b = Math.max(j - 1, a)
                        } else {
                            if (j == b) {
                                // new model should be after old one, and
                                // the old one is last in list.
                                models[j] = model
                                mObserver[model] = j
                                return old
                            } else
                                a = Math.min(j + 1, b)
                        }

                        var x = binarySearch(models, model, a, b)
                        if (x > j) {
                            x--
                        }

                        if (x == j) {
                            models[j] = model
                            mObserver[model] = j
                        } else {
                            models.removeAt(j)
                            mObserver.remove(model, j)

                            models.add(x, model)
                            mObserver.add(model, x)

                            // TODO: Use Observer#move(...) instead of remove & add. Note that
                            // none of actual observers support this thing.

                            // This happened to be too complex to support.
                            // mObserver.move(model, j, x);
                        }
                    }
                } else
                    throw IllegalStateException()
            } else if (validNew) {
                // add a new model to the list
                val x = binarySearch(models, model)
                models.add(x, model)
                mObserver.add(model, x)
            } else if (validOld) {
                // remove old model from the list
                val j = indexOf(models, old)
                if (j >= 0) {
                    models.removeAt(j)
                    mObserver.remove(model, j)
                } else
                    throw IllegalStateException()
            }

            return old
        } else {
            val j = modelsAll.size
            modelsAll.add(j, model)

            // update local list
            val validNew = validator(model)
            if (validNew) {
                // add a new model to the local list
                val x = binarySearch(models, model)
                models.add(x, model)
                mObserver.add(model, x)
            }

            return null
        }
    }

    fun remove(model: T) {
        remove(model.getKey())
    }

    fun remove(key: String) {
        val i = indexOf(modelsAll, key)
        if (i >= 0) {
            val old = modelsAll[i]
            modelsAll.removeAt(i)

            // remove old model from the list
            val j = indexOf(models, old.getKey())
            if (j >= 0) {
                models.removeAt(j)
                mObserver.remove(old, j)
            }
        }
    }

    fun reset() {
        models.clear()
        modelsAll.clear()
    }

    fun replaceAll(list: Collection<T>) {
        models.clear()
        modelsAll.clear()

        mObserverEnabled = false
        for (model in list) {
            put(model)
        }
        mObserverEnabled = true
        mObserver.avalanche()
    }

    @JvmOverloads
    fun refilter(avalanche: Boolean = false) {
        if (avalanche) {
            val list = ArrayList(modelsAll)
            replaceAll(list)
            return
        }

        for (i in models.indices.reversed()) {
            val old = models[i]
            if (validator(old).not()) {
                models.removeAt(i)
                mObserver.remove(old, i)
            }
        }

        for (model in modelsAll) {
            val j = indexOf(models, model)
            if (j < 0 && validator(model)) {
                // add a new model to the local list
                val x = binarySearch(models, model)
                models.add(x, model)
                mObserver.add(model, x)
            }
        }
    }

    fun indexOf(model: T): Int {
        return indexOf(modelsAll, model)
    }

    fun indexOf(list: List<T>, model: T): Int {
        return indexOf(list, model.getKey())
    }

    fun indexOf(list: List<T>, key: String): Int {
        val size = list.size
        return (0 until size).firstOrNull { TextUtils.equals(list[it].getKey(), key) }
                ?: -1
    }

    private fun binarySearch(list: List<T>, model: T): Int {
        return if (list.isEmpty()) 0 else binarySearch(list, model, 0, list.size - 1)
    }

    private fun binarySearch(list: List<T>, model: T, a: Int, b: Int): Int {
        if (a == b) {
            val old = list[a]
            val d = comparator.compare(model, old)
            return if (d > 0) a + 1 else a
        }

        val c = (a + b) / 2
        val old = list[c]
        val d = comparator.compare(model, old)
        return when {
            d == 0 -> c
            d < 0 -> binarySearch(list, model, a, Math.max(c - 1, 0))
            else -> binarySearch(list, model, Math.min(c + 1, b), b)
        }
    }

}
