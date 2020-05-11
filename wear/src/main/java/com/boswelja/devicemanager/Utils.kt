/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager

import android.app.NotificationManager
import android.content.Context
import com.boswelja.devicemanager.common.Compat

object Utils {

    /**
     * Set the system's current Interruption Filter state, or set silent mode if
     * Interruption Filter doesn't exist.
     * @param interruptionFilterOn Specify the new Interruption Filter state.
     * @return true if successfully set DnD mode, false otherwise
     */
    fun setInterruptionFilter(context: Context, interruptionFilterOn: Boolean): Boolean {
        if (interruptionFilterOn != Compat.isDndEnabled(context)) {
            try {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (interruptionFilterOn) {
                    notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
                } else {
                    notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                }
                return true
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        } else {
            return true
        }
        return false
    }
}
