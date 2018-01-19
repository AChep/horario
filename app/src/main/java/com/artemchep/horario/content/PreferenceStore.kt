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
package com.artemchep.horario.content

import android.app.Application
import android.content.Context
import android.support.annotation.CheckResult
import java.util.*
import kotlin.reflect.KClass

/**
 * @author Artem Chepurnoy
 */
abstract class PreferenceStore {

    private val store = HashMap<String, Preference>()
    private var listeners = ArrayList<OnPreferenceStoreChangeListener>()

    private val lock = java.lang.Object()
    private var loaded = false
    private var loader: Loader? = null

    protected abstract val preferenceName: String

    /**
     * Sets up the structure and default values
     * of the config.
     */
    protected abstract fun onCreatePreferenceMap(map: MutableMap<String, Preference>)

    /**
     * [Sets up config][.onCreatePreferenceMap] and loads previously
     * stored values from shared preferences. Should be called on [Application.onCreate]
     */
    fun load(context: Context) {
        store.clear()
        onCreatePreferenceMap(store)

        loader = Loader(context).apply { start() }
    }

    operator fun <T> get(key: String): T {
        synchronized(lock, {
            if (!loaded) lock.wait()
            return store[key]!!.value as T
        })
    }

    @CheckResult
    fun edit(context: Context) = Editor(this, context)

    fun addListener(l: OnPreferenceStoreChangeListener) = listeners.add(l)

    fun removeListener(l: OnPreferenceStoreChangeListener) = listeners.remove(l)

    /**
     * @author Artem Chepurnoy
     */
    interface OnPreferenceStoreChangeListener {
        fun onPreferenceStoreChange(context: Context, pref: Preference, old: Any)
    }

    /**
     * @author Artem Chepurnoy
     */
    inner class Loader(val context: Context) : Thread() {

        override fun run() {
            super.run()

            val map = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE).all
            for (pref in store.values) {
                val value = map[pref.key]
                if (value != null) pref.value = value
            }

            // Mark as loaded
            synchronized(lock, {
                loaded = true
                lock.notifyAll()
            })
        }

    }

    /**
     * @author Artem Chepurnoy
     */
    class Editor(private val mStore: PreferenceStore, private val mContext: Context) {

        private val list = ArrayList<Pair<String, Any>>()

        fun put(key: String, value: Any): Editor = also {
            list.add(Pair(key, value))
        }

        @JvmOverloads
        fun commit(listenerToIgnore: OnPreferenceStoreChangeListener? = null) {
            val listeners = mStore.listeners.filter { listenerToIgnore !== it }
            val editor = mContext
                    .getSharedPreferences(mStore.preferenceName, Context.MODE_PRIVATE)
                    .edit()
            for (diff in list) {
                val pref = mStore.store[diff.first]!!
                val old = pref.value

                // Check if value changed otherwise
                // just skip it
                if (diff.second == old) {
                    continue
                }

                pref.value = diff.second
                listeners.forEach { it.onPreferenceStoreChange(mContext, pref, old) }

                // Tell editor to put new value
                when {
                    pref.value is Boolean -> editor.putBoolean(pref.key, pref.value as Boolean)
                    pref.value is Int -> editor.putInt(pref.key, pref.value as Int)
                    pref.value is Float -> editor.putFloat(pref.key, pref.value as Float)
                    pref.value is String -> editor.putString(pref.key, pref.value as String)
                    pref.value is Long -> editor.putLong(pref.key, pref.value as Long)
                    else -> throw IllegalArgumentException("Unknown option\'s type.")
                }
            }
            editor.apply()
        }

    }

    /**
     * @author Artem Chepurnoy
     */
    class Preference private constructor() {

        lateinit var clazz: KClass<*>
        lateinit var key: String
        lateinit var value: Any

        /**
         * @author Artem Chepurnoy
         */
        class Builder<T : Any>(
                private val key: String,
                private val value: T
        ) {

            private val clazz: KClass<out T> = value::class

            @CheckResult
            fun build(): Preference {
                return Preference().apply {
                    key = this@Builder.key
                    clazz = this@Builder.clazz
                    value = this@Builder.value
                }
            }

        }

    }

}
