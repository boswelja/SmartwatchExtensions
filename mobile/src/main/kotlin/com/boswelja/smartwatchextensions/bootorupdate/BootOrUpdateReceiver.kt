/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
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
