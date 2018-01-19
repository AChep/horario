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
package com.artemchep.horario.billing;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

import org.solovyev.android.checkout.Sku;

/**
 * @author Artem Chepurnoy
 */
public class SkuUi {

    private static final long MICRO = 1_000_000; // defines how much 'micro' is

    @NonNull
    public final Sku sku;

    @DrawableRes
    public final int icon;

    private final boolean mIsPurchased;
    private final String mTitle;

    public SkuUi(@NonNull Sku sku, @DrawableRes int icon, boolean isPurchased) {
        this.sku = sku;
        this.icon = icon;
        mIsPurchased = isPurchased;
        mTitle = sku.title.substring(0, sku.title.indexOf(" ("));
    }

    public String getTitle() {
        return mTitle != null ? mTitle : sku.title;
    }

    /**
     * @return the price of the sku in {@link #getPriceCurrency() currency}.
     * @see #getPriceCurrency()
     */
    @NonNull
    public String getPriceAmount() {
        long amountMicro = sku.detailedPrice.amount;
        if (amountMicro % MICRO == 0) {
            // Format it 'as int' number to
            // get rid of unused comma.
            long amount = amountMicro / MICRO;
            return String.valueOf(amount);
        }

        double amount = (double) amountMicro / MICRO;
        return String.valueOf(amount);
    }

    /**
     * @return the currency of the price.
     * @see #getPriceAmount()
     */
    @NonNull
    public String getPriceCurrency() {
        return sku.detailedPrice.currency;
    }

    /**
     * @return {@code true} if the sku is purchased,
     * {@code false} otherwise.
     */
    public boolean isPurchased() {
        return mIsPurchased;
    }

}