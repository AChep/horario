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
package com.artemchep.horario

import com.artemchep.horario.content.PreferenceStore

typealias Builder<T> = PreferenceStore.Preference.Builder<T>

/**
 * @author Artem Chepurnoy
 */
object Config : PreferenceStore() {

    // Prompt
    const val KEY_PROMPT_SUBJECT_EDIT_PALETTE = "prompt_subject_edit_palette"
    // Interface
    const val KEY_UI_THEME = "ui_theme"
    // Debug
    const val KEY_DEBUG = "debug"
    const val KEY_DEBUG_SYNC_SCHEDULES = "debug_sync_schedules"
    const val THEME_LIGHT = 0
    const val THEME_DARK = 1
    const val THEME_BLACK = 2

    public override val preferenceName: String
        get() = "config"

    override fun onCreatePreferenceMap(map: MutableMap<String, Preference>) {
        // Prompt
        KEY_PROMPT_SUBJECT_EDIT_PALETTE.let { map[it] = Builder(it, false).build() }
        // Interface
        KEY_UI_THEME.let { map[it] = Builder(it, THEME_LIGHT).build() }
    }

}
