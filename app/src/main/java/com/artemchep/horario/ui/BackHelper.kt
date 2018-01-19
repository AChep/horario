package com.artemchep.horario.ui

import android.content.Context
import android.os.SystemClock
import es.dmoral.toasty.Toasty

/**
 * @author Artem Chepurnoy
 */
class BackHelper<T> {

    companion object {
        private val DELAY = 2000L
    }

    private var prevPressTime: Long = 0

    fun onBackPressed(context: Context, origin: T, model: T): Boolean {
        val now = SystemClock.uptimeMillis()
        return if (now - prevPressTime > DELAY) {
            prevPressTime = now

            Toasty.normal(context, "Press back again").show()
            true
        } else false
    }

}
