package com.artemchep.horario.extensions

import android.support.v4.view.ViewCompat
import android.view.View

fun <T : View> T.withOnClick(l: View.OnClickListener?): T {
    setOnClickListener(l)
    return this
}

var View.transitionNameCompat: String
    set(name) = ViewCompat.setTransitionName(this, name)
    get() = ViewCompat.getTransitionName(this)
