/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.dndsync

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Build

abstract class DnDLocalChangeReceiver : BroadcastReceiver() {

    abstract fun onDnDChanged(dndEnabled: Boolean)

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED -> {
                if (context != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val dndEnabled = Utils.isDnDEnabled(context)
                    onDnDChanged(dndEnabled)
                }
            }
            AudioManager.RINGER_MODE_CHANGED_ACTION -> {
                if (context != null) {
                    val ringerMode = intent.getIntExtra(AudioManager.EXTRA_RINGER_MODE, AudioManager.RINGER_MODE_NORMAL)
                    val dndEnabled = (ringerMode == AudioManager.RINGER_MODE_SILENT) or (ringerMode == AudioManager.RINGER_MODE_VIBRATE)
                    onDnDChanged(dndEnabled)
                }
            }
        }
    }

    companion object {
        fun registerReceiver(context: Context, receiver: DnDLocalChangeReceiver) {
            val intentFilter = IntentFilter().apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    addAction(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED)
                } else {
                    addAction(AudioManager.RINGER_MODE_CHANGED_ACTION)
                }
            }
            context.registerReceiver(receiver, intentFilter)
        }
    }
}
