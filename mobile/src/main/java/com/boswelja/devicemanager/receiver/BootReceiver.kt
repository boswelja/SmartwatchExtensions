/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.watchconnectionmanager.WatchConnectionService

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            onBootCompleted(context)
        }
    }

    private fun onBootCompleted(context: Context?) {
        Intent(context!!.applicationContext, WatchConnectionService::class.java).apply {
            action = Intent.ACTION_BOOT_COMPLETED
        }.also {
            Compat.startForegroundService(context.applicationContext, it)
        }
    }
}
