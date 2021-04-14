package com.boswelja.smartwatchextensions.bootorupdate

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import timber.log.Timber

class BootOrUpdateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Timber.i("Received a broadcast")
        when (val broadcastAction = intent?.action) {
            Intent.ACTION_MY_PACKAGE_REPLACED, Intent.ACTION_BOOT_COMPLETED -> {
                Timber.i("Starting BootOrUpdateHandlerService")
                Intent(context!!.applicationContext, BootOrUpdateHandlerService::class.java)
                    .apply { action = broadcastAction }
                    .also { ContextCompat.startForegroundService(context.applicationContext, it) }
            }
        }
    }
}
