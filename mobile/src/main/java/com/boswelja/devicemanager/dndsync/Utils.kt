/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.dndsync

import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.os.Build
import androidx.annotation.RequiresApi

object Utils {

    @RequiresApi(Build.VERSION_CODES.M)
    fun isDnDEnabled(context: Context): Boolean {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val currentInterruptFilter = notificationManager.currentInterruptionFilter
        return (currentInterruptFilter == NotificationManager.INTERRUPTION_FILTER_ALARMS) or
                (currentInterruptFilter == NotificationManager.INTERRUPTION_FILTER_PRIORITY) or
                (currentInterruptFilter == NotificationManager.INTERRUPTION_FILTER_NONE)
    }

    private fun isInSilentMode(context: Context): Boolean {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return (audioManager.ringerMode == AudioManager.RINGER_MODE_SILENT) or (audioManager.ringerMode == AudioManager.RINGER_MODE_VIBRATE)
    }

    fun isDnDEnabledCompat(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            isDnDEnabled(context)
        } else {
            isInSilentMode(context)
        }
    }
}
