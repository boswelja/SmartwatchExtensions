/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.common

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.preference.PreferenceManager

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent!!.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            if (prefs.getBoolean(PreferenceKey.DND_SYNC_SEND_KEY, false)) {
                val serviceIntent = Intent(context, DnDLocalChangeListener::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context!!.startForegroundService(serviceIntent)
                } else {
                    context!!.startService(serviceIntent)
                }
            }
        }
    }
}