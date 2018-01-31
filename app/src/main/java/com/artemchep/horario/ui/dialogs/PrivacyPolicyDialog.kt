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
package com.artemchep.horario.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import com.afollestad.materialdialogs.MaterialDialog
import com.artemchep.horario.utils.HtmlUtils
import com.artemchep.horario.utils.RawReader
import com.artemchep.horario.R

/**
 * @author Artem Chepurnoy
 */
class PrivacyPolicyDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Load icon
        val a = activity!!.theme.obtainStyledAttributes(intArrayOf(R.attr.icon_shield))
        val iconDrawableRes = a.getResourceId(0, 0)
        a.recycle()

        val source = RawReader.readText(context!!, R.raw.privacy_policy)
        val privacyPolicy = HtmlUtils.fromLegacyHtml(source)

        return MaterialDialog.Builder(context!!)
                .iconRes(iconDrawableRes)
                .title(R.string.dialog_privacy_policy)
                .content(privacyPolicy)
                .negativeText(R.string.dialog_close)
                .build()
    }

}
