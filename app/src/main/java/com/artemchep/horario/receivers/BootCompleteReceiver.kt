package com.artemchep.horario.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import timber.log.Timber
import com.artemchep.horario.services.AlarmRestoreOnRebootService

/**
 * @author Artem Chepurnoy
 */
class BootCompleteReceiver : BroadcastReceiver() {

    companion object {
        private val TAG = "BootCompleteReceiver"
    }

    override fun onReceive(ctx: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            "android.intent.action.QUICKBOOT_POWERON" -> {
                val i = Intent(ctx, AlarmRestoreOnRebootService::class.java)
                ctx.startService(i)
            }
            else -> {
                Timber.tag(TAG).w("Received unknown action=${intent.action}!")
            }
        }
    }

}