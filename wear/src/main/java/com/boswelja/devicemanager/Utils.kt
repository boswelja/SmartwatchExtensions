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
import android.content.pm.PackageManager
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.common.References
import com.boswelja.devicemanager.phoneconnectionmanager.References.PHONE_ID_KEY
import com.google.android.gms.wearable.Wearable

object Utils {

    fun checkDnDAccess(context: Context): Boolean =
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                    .isNotificationPolicyAccessGranted

    fun launchMobileApp(context: Context, key: String) {
        val nodeId = PreferenceManager.getDefaultSharedPreferences(context).getString(PHONE_ID_KEY, "")
        if (nodeId != null) {
            Wearable.getMessageClient(context)
                    .sendMessage(nodeId, References.REQUEST_LAUNCH_APP_PATH, key.toByteArray(Charsets.UTF_8))
        }
    }

    /**
     * Set the system's current Interruption Filter state, or set silent mode if
     * Interruption Filter doesn't exist.
     * @param interruptionFilterOn Specify the new Interruption Filter state.
     */
    fun setInterruptionFilter(context: Context, interruptionFilterOn: Boolean) {
        if (interruptionFilterOn != Compat.interruptionFilterEnabled(context)) {
            try {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (interruptionFilterOn) {
                    notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
                } else {
                    notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
                val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                prefs.edit().putBoolean(PreferenceKey.DND_SYNC_TO_WATCH_KEY, false).apply()
            }
        }
    }

    fun isAppInstalled(packageManager: PackageManager, packageName: String): Boolean {
        try {
            packageManager.getApplicationInfo(packageName, 0)
        } catch (_: Exception) {
            return false
        }
        return true
    }
}
