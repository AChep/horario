/*
 * Copyright (C) 2017 Artem Chepurnoy <artemchep@gmail.com>
 */
package com.artemchep.horario.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.text.Spanned
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.artemchep.basic.utils.HtmlUtils
import com.artemchep.horario.Binfo
import com.artemchep.horario.R
import com.google.firebase.crash.FirebaseCrash
import es.dmoral.toasty.Toasty

/**
 * @author Artem Chepurnoy
 */
class AboutDialog : DialogFragment() {

    private var timestampToast: Toast? = null

    private fun getFormattedVersionName(context: Context): Spanned {
        val pm = context.packageManager
        val packageName = context.packageName
        var versionName: String
        try {
            val info = pm.getPackageInfo(packageName, 0)
            versionName = info.versionName

            // Make the info part of version name a bit smaller.
            if (versionName.indexOf('-') >= 0) {
                versionName = versionName.replaceFirst("-".toRegex(), "<small>-") + "</small>"
            }
        } catch (e: PackageManager.NameNotFoundException) {
            FirebaseCrash.report(Exception("Own package not found.", e))
            versionName = "N/A"
        }

        // TODO: Get the tint color from current theme.
        val color = 0xFF888888.toInt()

        val res = context.resources
        val html = res.getString(R.string.dialog_about_title,
                res.getString(R.string.app_name),
                versionName,
                Integer.toHexString(Color.red(color))
                        + Integer.toHexString(Color.green(color))
                        + Integer.toHexString(Color.blue(color)))
        return HtmlUtils.fromLegacyHtml(html)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Load icon
        val a = activity!!.theme.obtainStyledAttributes(intArrayOf(R.attr.icon_information_outline))
        val iconDrawableRes = a.getResourceId(0, 0)
        a.recycle()

        // Content
        val year = Binfo.TIME_STAMP_YEAR
        val credits = getString(R.string.dialog_about_credits)
        val src = getString(R.string.dialog_about_message, credits, year)
        val title = getFormattedVersionName(context!!)
        val message = HtmlUtils.fromLegacyHtml(src)

        val md = MaterialDialog.Builder(context!!)
                .iconRes(iconDrawableRes)
                .title(title)
                .content(message)
                .negativeText(R.string.dialog_close)
                .build()
        md.titleView.setOnClickListener { _ ->
            timestampToast?.cancel()
            timestampToast = Toasty.info(context!!, Binfo.TIME_STAMP).apply { show() }
        }

        return md
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        timestampToast?.cancel()
        timestampToast = null
    }

}
