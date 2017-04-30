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
package com.artemchep.horario.interfaces;

import android.net.Uri;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

/**
 * Base class for simple virtual coins.
 *
 * @author Artem Chepurnoy
 */
public interface ICoin {

    @DrawableRes
    int getIconResource();

    /**
     * @return the string resource of the name of this coin. To get string name,
     * simply do {@code getResources().getString(nameRes)}.
     */
    @StringRes
    int getNameResource();

    /**
     * @return The receiver's key. It may be an email or just "any string".
     */
    @NonNull
    String getPaymentKey();

    /**
     * @return The default amount of money.
     */
    double getPaymentAmount();

    /**
     * @param amount the amount to send, or if {@code <= 0} blank field.
     * @return Uri to page through which you can sends me moneys.
     * @see com.artemchep.horario.utils.CoinUtils#getPaymentIntent(ICoin)
     * @see com.artemchep.horario.utils.CoinUtils#getPaymentIntent(ICoin, double)
     */
    @NonNull
    Uri getPaymentUri(double amount);

    /**
     * @return Uri to page that shows my current money.
     * @see #getUriWhatIsIt()
     * @see #getUriTutorial()
     */
    @Nullable
    Uri getUriBrowseWallet();

    /**
     * @return Uri to page that explains the coin.
     * @see #getUriBrowseWallet()
     * @see #getUriTutorial()
     */
    @NonNull
    Uri getUriWhatIsIt();

    /**
     * @return Uri to page that explains how to use that coin: send / receive money etc.
     * @see #getUriWhatIsIt()
     * @see #getUriBrowseWallet()
     */
    @NonNull
    Uri getUriTutorial();

}
