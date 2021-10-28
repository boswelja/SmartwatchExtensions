package com.boswelja.smartwatchextensions

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.boswelja.smartwatchextensions.batterysync.Utils.BATTERY_STATS_NOTI_CHANNEL_ID
import com.boswelja.smartwatchextensions.bootorupdate.BOOT_OR_UPDATE_NOTI_CHANNEL_ID
import com.boswelja.smartwatchextensions.dndsync.DND_SYNC_NOTI_CHANNEL_ID
import com.boswelja.smartwatchextensions.messages.MESSAGE_NOTIFICATION_CHANNEL_ID
import com.boswelja.smartwatchextensions.proximity.SeparationObserverService.Companion.OBSERVER_NOTI_CHANNEL_ID
import com.boswelja.smartwatchextensions.proximity.SeparationObserverService.Companion.SEPARATION_NOTI_CHANNEL_ID

/**
 * A helper class for managing notification channels.
 */
object NotificationChannelHelper {

    /**
     * Create a notification channel for battery stats notifications.
     */
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

    /**
     * Create a notification channel for separation observer status notifications.
     */
    fun createForSeparationObserver(context: Context, notificationManager: NotificationManager) {
        if (notificationManager.getNotificationChannel(OBSERVER_NOTI_CHANNEL_ID) == null) {
            NotificationChannel(
                OBSERVER_NOTI_CHANNEL_ID,
                context.getString(R.string.proximity_observer_noti_channel_title),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                enableLights(false)
                enableVibration(false)
                setShowBadge(false)
            }.also {
                notificationManager.createNotificationChannel(it)
            }
        }
    }

    /**
     * Create a notification channel for Separation Alerts.
     */
    fun createForSeparationNotis(context: Context, notificationManager: NotificationManager) {
        if (notificationManager.getNotificationChannel(SEPARATION_NOTI_CHANNEL_ID) == null) {
            NotificationChannel(
                SEPARATION_NOTI_CHANNEL_ID,
                context.getString(R.string.separation_noti_channel_title),
                NotificationManager.IMPORTANCE_HIGH
            ).also {
                notificationManager.createNotificationChannel(it)
            }
        }
    }

    /**
     * Create a notification channel for DnD Sync status notifications.
     */
    fun createForDnDSync(context: Context, notificationManager: NotificationManager) {
        if (notificationManager.getNotificationChannel(DND_SYNC_NOTI_CHANNEL_ID) ==
            null
        ) {
            NotificationChannel(
                DND_SYNC_NOTI_CHANNEL_ID,
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

    /**
     * Create a notification channel for messages from the system.
     */
    fun createForSystemMessages(context: Context, notificationManager: NotificationManager) {
        NotificationChannel(
            MESSAGE_NOTIFICATION_CHANNEL_ID,
            context.getString(R.string.messages_noti_channel_label),
            NotificationManager.IMPORTANCE_DEFAULT
        ).also {
            notificationManager.createNotificationChannel(it)
        }
    }
}
