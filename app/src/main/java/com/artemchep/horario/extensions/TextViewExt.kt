package com.artemchep.horario.extensions

import android.view.View
import android.widget.TextView
import org.sufficientlysecure.htmltextview.HtmlTextView

/**
 * [Sets text][TextView.setText] to TextView and, if the text is `null` or empty,
 * hides the view.
 */
fun <T : TextView> T.setTextExclusive(text: String?) = setTextExclusive(text, this)

fun <T : TextView> T.setTextExclusive(text: String?, view: View) {
    this.text = text
    view.visibility = if (text.isNullOrEmpty()) View.GONE else View.VISIBLE
}

fun <T : HtmlTextView> T.setHtmlExclusive(text: String?): T = setHtmlExclusive(text, this)

fun <T : HtmlTextView> T.setHtmlExclusive(text: String?, view: View): T {
    view.visibility = if (text.isNullOrBlank()) {
        setHtml("") // clean-up html
        View.GONE
    } else {
        setHtml(text!!)
        View.VISIBLE
    }
    return this
}
