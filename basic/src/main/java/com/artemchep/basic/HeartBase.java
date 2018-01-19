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
package com.artemchep.basic;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

import org.solovyev.android.checkout.Billing;

/**
 * @author Artem Chepurnoy
 */
public abstract class HeartBase extends Application {

    @NonNull
    public static Billing getBilling(@NonNull Context context) {
        HeartBase application = (HeartBase) context.getApplicationContext();
        return application.getBilling();
    }

    protected abstract Billing getBilling();

}
