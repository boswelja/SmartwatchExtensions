/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.common

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat

/*
 * Compatibility layer to aid support for different Android versions
 */
object Compat {

    /**
     * Starts a service in the foreground.
     * @param intent Intent for the service.
     */
    fun startForegroundService(context: Context, intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    /**
     * Checks whether Do not Disturb is currently active.
     * Will fall back to silent / vibrate on older Android versions
     * @return true if DnD is enabled, false otherwise.
     */
    fun isDndEnabled(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val currentInterruptFilter = notificationManager.currentInterruptionFilter
            (currentInterruptFilter == NotificationManager.INTERRUPTION_FILTER_ALARMS) ||
                    (currentInterruptFilter == NotificationManager.INTERRUPTION_FILTER_PRIORITY) ||
                    (currentInterruptFilter == NotificationManager.INTERRUPTION_FILTER_NONE)
        } else {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            (audioManager.ringerMode == AudioManager.RINGER_MODE_SILENT) ||
                    (audioManager.ringerMode == AudioManager.RINGER_MODE_VIBRATE)
        }
    }

    /**
     * Checks whether Wearable Extensions has permission to set the device's Do not Disturb state.
     * @param context [Context].
     * @return true if we can set the Do not Disturb state, false otherwise
     */
    fun canSetDnD(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val notiManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notiManager.isNotificationPolicyAccessGranted
        } else {
            true
        }
    }

    /**
     * Checks whether notifications are enabled for this app.
     * @param channelId ID of the notification channel to check. Ignored on platforms below API26
     * and can be left null to check whether notifications are enabled overall for this app.
     * @return true if notifications are enabled, false otherwise.
     */
    fun areNotificationsEnabled(context: Context, channelId: String? = null): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !channelId.isNullOrBlank()) {
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).also {
                val channel = it.getNotificationChannel(channelId)
                return channel != null && channel.importance != NotificationManager.IMPORTANCE_NONE
            }
        } else {
            return NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }
}
