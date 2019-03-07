/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.common

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.preference.PreferenceManager
import androidx.core.app.NotificationCompat
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

abstract class BatteryUpdateListener : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent?) {
        if (messageEvent?.path == References.BATTERY_STATUS_PATH) {
            val preferenceManager = PreferenceManager.getDefaultSharedPreferences(this)
            val message = String(messageEvent.data, Charsets.UTF_8)
            val messageSplit = message.split("|")
            val percent = messageSplit[0].toInt()
            val charging = messageSplit[1] == "true"
            preferenceManager.edit().putInt(References.BATTERY_PERCENT_KEY, percent).apply()

            if (preferenceManager.getBoolean(PreferenceKey.BATTERY_FULL_CHARGE_NOTI_KEY, false) &&
                    !preferenceManager.getBoolean(PreferenceKey.BATTERY_CHARGED_NOTI_ACKNOWLEDGED, false) &&
                    percent > 90 &&
                    charging) {
                sendChargedNoti()
            }

            if (!charging) {
                preferenceManager.edit().putBoolean(PreferenceKey.BATTERY_CHARGED_NOTI_ACKNOWLEDGED, false).apply()
            }

            onBatteryUpdate(percent, charging)
        }
    }

    private fun sendChargedNoti() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notiChannelKey = getString(R.string.device_charged_noti_channel_key)
        val companionDeviceName = getString(R.string.companion_device_type)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationManager.getNotificationChannel(notiChannelKey) == null) {
            val channel = NotificationChannel(notiChannelKey, getString(R.string.device_charged_noti_channel_name, companionDeviceName), NotificationManager.IMPORTANCE_HIGH)
            channel.enableVibration(true)
            notificationManager.createNotificationChannel(channel)
        }
        val noti = NotificationCompat.Builder(this, notiChannelKey)
        noti.setSmallIcon(R.drawable.battery_full)
        noti.setContentTitle(getString(R.string.device_charged_noti_title, companionDeviceName))
        noti.setContentText(getString(R.string.device_charged_noti_desc, companionDeviceName))
        noti.setLocalOnly(true)

        val intent = Intent(this, NotificationDismissedService::class.java)
        val deleteIntent = PendingIntent.getService(this, 0, intent, 0)
        noti.setDeleteIntent(deleteIntent)

        notificationManager.notify(References.NOTIFICATION_ID, noti.build())
    }

    abstract fun onBatteryUpdate(percent: Int, charging: Boolean)
}