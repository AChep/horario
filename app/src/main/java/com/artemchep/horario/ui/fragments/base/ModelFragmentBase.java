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
package com.artemchep.horario.ui.fragments.base;

import android.support.v7.view.ActionMode;

import com.artemchep.horario.aggregator.Filter;
import com.artemchep.horario.models.Model;
import com.artemchep.horario.ui.fragments.master.MasterFragment;

/**
 * @author Artem Chepurnoy
 */
public abstract class ModelFragmentBase<T extends Model> extends MasterFragment implements
        ActionMode.Callback,
        Filter.OnFilterChangedListener {

}
