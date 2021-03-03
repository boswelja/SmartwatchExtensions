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
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import timber.log.Timber

/*
 * Compatibility layer to aid support for different Android versions
 */
object Compat {

    /**
     * Set the system's current Interruption Filter state, or set silent mode if Interruption Filter
     * doesn't exist.
     * @param interruptionFilterOn Specify the new Interruption Filter state.
     * @return true if interrupt filter was set successfully, false otherwise.
     */
    fun setInterruptionFilter(context: Context, interruptionFilterOn: Boolean): Boolean {
        if (interruptionFilterOn != isDndEnabled(context)) {
            try {
                val notificationManager =
                    context.getSystemService(
                        Context.NOTIFICATION_SERVICE
                    ) as NotificationManager
                if (interruptionFilterOn) {
                    notificationManager.setInterruptionFilter(
                        NotificationManager.INTERRUPTION_FILTER_PRIORITY
                    )
                } else {
                    notificationManager.setInterruptionFilter(
                        NotificationManager.INTERRUPTION_FILTER_ALL
                    )
                }
                return true
            } catch (e: SecurityException) {
                Timber.e(e)
            }
        } else {
            return true
        }
        return false
    }

    /**
     * Checks whether Do not Disturb is currently active. Will fall back to silent / vibrate on
     * older Android versions
     * @return true if DnD is enabled, false otherwise.
     */
    fun isDndEnabled(context: Context): Boolean {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val currentInterruptFilter = notificationManager.currentInterruptionFilter
        return (currentInterruptFilter == NotificationManager.INTERRUPTION_FILTER_ALARMS) ||
            (currentInterruptFilter == NotificationManager.INTERRUPTION_FILTER_PRIORITY) ||
            (currentInterruptFilter == NotificationManager.INTERRUPTION_FILTER_NONE)
    }

    /**
     * Checks whether Wearable Extensions has permission to set the device's Do not Disturb state.
     * @param context [Context].
     * @return true if we can set the Do not Disturb state, false otherwise
     */
    fun canSetDnD(context: Context): Boolean {
        val notiManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notiManager.isNotificationPolicyAccessGranted
    }

    /**
     * Checks whether notifications are enabled for this app.
     * @param channelId ID of the notification channel to check. Ignored on platforms below API26.
     * Can be left null to check global app notifications.
     * @return true if notifications are enabled, false otherwise.
     */
    fun areNotificationsEnabled(context: Context, channelId: String? = null): Boolean {
        val notificationManagerCompat = NotificationManagerCompat.from(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !channelId.isNullOrBlank()) {
            notificationManagerCompat.getNotificationChannel(channelId).let {
                return it != null && it.importance != NotificationManager.IMPORTANCE_NONE
            }
        } else {
            return notificationManagerCompat.areNotificationsEnabled()
        }
    }
}
