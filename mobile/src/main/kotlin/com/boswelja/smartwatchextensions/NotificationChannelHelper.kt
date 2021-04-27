package com.boswelja.smartwatchextensions

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.boswelja.smartwatchextensions.batterysync.Utils.BATTERY_STATS_NOTI_CHANNEL_ID
import com.boswelja.smartwatchextensions.bootorupdate.BootOrUpdateHandlerService
import com.boswelja.smartwatchextensions.common.dndsync.References
import com.boswelja.smartwatchextensions.messages.MessageHandler

object NotificationChannelHelper {

    fun createForBatteryStats(context: Context, notificationManager: NotificationManager) {
        if (notificationManager.getNotificationChannel(
                BATTERY_STATS_NOTI_CHANNEL_ID
            ) == null
        ) {
            NotificationChannel(
                BATTERY_STATS_NOTI_CHANNEL_ID,
                context.getString(R.string.noti_channel_watch_charged_title),
                NotificationManager.IMPORTANCE_HIGH
            )
                .apply {
                    enableLights(false)
                    enableVibration(true)
                    setShowBadge(true)
                }
                .also { notificationManager.createNotificationChannel(it) }
        }
    }

    fun createForDnDSync(context: Context, notificationManager: NotificationManager) {
        if (notificationManager.getNotificationChannel(References.DND_SYNC_NOTI_CHANNEL_ID) ==
            null
        ) {
            NotificationChannel(
                References.DND_SYNC_NOTI_CHANNEL_ID,
                context.getString(R.string.noti_channel_dnd_sync_title),
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

    fun createForBootOrUpdate(context: Context, notificationManager: NotificationManager) {
        if (notificationManager.getNotificationChannel(
                BootOrUpdateHandlerService.BOOT_OR_UPDATE_NOTI_CHANNEL_ID
            ) == null
        ) {
            NotificationChannel(
                BootOrUpdateHandlerService.BOOT_OR_UPDATE_NOTI_CHANNEL_ID,
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

    fun createForSystemMessages(context: Context, notificationManager: NotificationManager) {
        NotificationChannel(
            MessageHandler.MESSAGE_NOTIFICATION_CHANNEL_ID,
            context.getString(R.string.messages_noti_channel_label),
            NotificationManager.IMPORTANCE_DEFAULT
        ).also {
            notificationManager.createNotificationChannel(it)
        }
    }
}
