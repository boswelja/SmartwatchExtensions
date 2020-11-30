/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.boswelja.devicemanager.batterysync.WatchBatteryUpdateReceiver
import com.boswelja.devicemanager.bootorupdate.BootOrUpdateHandlerService
import com.boswelja.devicemanager.common.dndsync.References
import com.boswelja.devicemanager.messages.MessageHandler

@RequiresApi(Build.VERSION_CODES.O)
object NotificationChannelHelper {

    fun createForBatteryCharged(context: Context, notificationManager: NotificationManager) {
        if (notificationManager.getNotificationChannel(
                WatchBatteryUpdateReceiver.BATTERY_CHARGED_NOTI_CHANNEL_ID
            ) == null
        ) {
            NotificationChannel(
                WatchBatteryUpdateReceiver.BATTERY_CHARGED_NOTI_CHANNEL_ID,
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
        if (notificationManager.getNotificationChannel(
                MessageHandler.MESSAGE_NOTIFICATION_CHANNEL_ID
            ) == null
        ) {
            NotificationChannel(
                MessageHandler.MESSAGE_NOTIFICATION_CHANNEL_ID,
                context.getString(R.string.messages_noti_channel_label),
                NotificationManager.IMPORTANCE_DEFAULT
            ).also {
                notificationManager.createNotificationChannel(it)
            }
        }
    }
}
