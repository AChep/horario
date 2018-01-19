package com.artemchep.horario.extensions

import android.support.v7.view.menu.ActionMenuItemView
import android.support.v7.widget.ActionMenuView
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.TextView
import org.sufficientlysecure.htmltextview.HtmlTextView

fun Toolbar.findMenuItemView(itemId: Int): ActionMenuItemView? {
    var itemView: ActionMenuItemView? = null

    menu_item_search@ for (i in 0 until childCount) {
        val view = getChildAt(i)
        if (view is ActionMenuView) {
            // Check all menu items
            for (j in 0 until view.childCount) {
                itemView = view.getChildAt(j) as ActionMenuItemView
                if (itemView.itemData.itemId == itemId) {
                    break@menu_item_search
                }
            }
        }
    }

    return itemView
}
