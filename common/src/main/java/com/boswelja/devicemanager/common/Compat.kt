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

/**
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
            } catch (ignored: SecurityException) { }
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
}
