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
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.preference.PreferenceManager
import androidx.core.app.NotificationCompat
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

abstract class BatteryUpdateListener : WearableListenerService() {

    private lateinit var prefs: SharedPreferences

    override fun onMessageReceived(messageEvent: MessageEvent?) {
        if (messageEvent?.path == References.BATTERY_STATUS_PATH) {
            prefs = PreferenceManager.getDefaultSharedPreferences(this)
            val message = String(messageEvent.data, Charsets.UTF_8)
            val messageSplit = message.split("|")
            val percent = messageSplit[0].toInt()
            val charging = messageSplit[1] == true.toString()
            prefs.edit().putInt(PreferenceKey.BATTERY_PERCENT_KEY, percent).apply()

            if (prefs.getBoolean(PreferenceKey.BATTERY_FULL_CHARGE_NOTI_KEY, false) &&
                    !prefs.getBoolean(PreferenceKey.BATTERY_CHARGED_NOTI_SENT, false) &&
                    percent > 90 &&
                    charging) {
                sendChargedNoti()
            }

            if (!charging) {
                prefs.edit().putBoolean(PreferenceKey.BATTERY_CHARGED_NOTI_SENT, false).apply()
            }

            onBatteryUpdate(percent, charging)
        }
    }

    private fun sendChargedNoti() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val companionDeviceName = getString(R.string.companion_device_type)

        val noti = NotificationCompat.Builder(this, References.BATTERY_CHARGED_NOTI_CHANEL_ID)
                .setSmallIcon(R.drawable.battery_full)
                .setContentTitle(getString(R.string.device_charged_noti_title, companionDeviceName))
                .setContentText(getString(R.string.device_charged_noti_desc, companionDeviceName))
                .setLocalOnly(true)
                .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationManager.getNotificationChannel(References.BATTERY_CHARGED_NOTI_CHANEL_ID) == null) {
            val channel = NotificationChannel(
                    References.BATTERY_CHARGED_NOTI_CHANEL_ID,
                    getString(R.string.device_charged_noti_channel_name, getString(R.string.companion_device_type)),
                    NotificationManager.IMPORTANCE_HIGH).apply {
                enableVibration(true)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(AtomicCounter.getInt(), noti)
        prefs.edit().putBoolean(PreferenceKey.BATTERY_CHARGED_NOTI_SENT, true).apply()
    }

    abstract fun onBatteryUpdate(percent: Int, charging: Boolean)
}