package com.boswelja.smartwatchextensions

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.boswelja.smartwatchextensions.bootorupdate.BOOT_OR_UPDATE_NOTI_CHANNEL_ID

/**
 * A helper class for managing notification channels.
 */
object NotificationChannelHelper {

    /**
     * Create a notification channel for Boot / Update status notifications.
     */
    fun createForBootOrUpdate(context: Context, notificationManager: NotificationManager) {
        if (notificationManager.getNotificationChannel(
                BOOT_OR_UPDATE_NOTI_CHANNEL_ID
            ) == null
        ) {
            NotificationChannel(
                BOOT_OR_UPDATE_NOTI_CHANNEL_ID,
                context.getString(R.string.noti_channel_boot_or_update_title),
                NotificationManager.IMPORTANCE_LOW
            )
                .apply {
                    enableLights(false)
                    enableVibration(false)
                    setShowBadge(false)
                }
                .also { notificationManager.createNotificationChannel(it) }
        }
    }
}
