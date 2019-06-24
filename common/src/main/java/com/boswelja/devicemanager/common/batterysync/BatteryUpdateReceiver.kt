/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.common.batterysync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.common.R
import com.boswelja.devicemanager.common.batterysync.References.BATTERY_STATUS_PATH
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

abstract class BatteryUpdateReceiver : WearableListenerService() {

    lateinit var sharedPreferences: SharedPreferences

    abstract fun shouldNotifyDeviceCharged(): Boolean
    abstract fun onBatteryUpdate(percent: Int, charging: Boolean)

    override fun onMessageReceived(messageEvent: MessageEvent?) {
        if (messageEvent?.path == BATTERY_STATUS_PATH) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            val message = String(messageEvent.data, Charsets.UTF_8)
            val messageSplit = message.split("|")
            val percent = messageSplit[0].toInt()
            val charging = messageSplit[1] == true.toString()
            sharedPreferences.edit()
                    .putLong(PreferenceKey.BATTERY_SYNC_LAST_WHEN_KEY, System.currentTimeMillis())
                    .putInt(PreferenceKey.BATTERY_PERCENT_KEY, percent)
                    .apply()

            val shouldNotifyDeviceCharged = shouldNotifyDeviceCharged()
            if (charging && shouldNotifyDeviceCharged) {
                val chargeThreshold = sharedPreferences.getInt(PreferenceKey.BATTERY_CHARGE_THRESHOLD_KEY, 90)
                if (percent >= chargeThreshold && !sharedPreferences.getBoolean(PreferenceKey.BATTERY_CHARGED_NOTI_SENT, false)) {
                    sendChargedNoti(chargeThreshold)
                }
            }

            if (!charging && sharedPreferences.getBoolean(PreferenceKey.BATTERY_CHARGED_NOTI_SENT, false)) {
                sharedPreferences.edit().putBoolean(PreferenceKey.BATTERY_CHARGED_NOTI_SENT, false).apply()
                if (shouldNotifyDeviceCharged) cancelChargedNoti()
            }

            onBatteryUpdate(percent, charging)
        }
    }

    private fun sendChargedNoti(chargeThreshold: Int) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val companionDeviceName = getString(R.string.companion_device_type)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationManager.getNotificationChannel(BATTERY_CHARGED_NOTI_CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                    BATTERY_CHARGED_NOTI_CHANNEL_ID,
                    getString(R.string.device_charged_noti_channel_name, getString(R.string.companion_device_type)),
                    NotificationManager.IMPORTANCE_HIGH).apply {
                enableVibration(true)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val noti = NotificationCompat.Builder(this, BATTERY_CHARGED_NOTI_CHANNEL_ID)
                .setSmallIcon(R.drawable.battery_full)
                .setContentTitle(getString(R.string.device_charged_noti_title, companionDeviceName))
                .setContentText(getString(R.string.device_charged_noti_desc).format(companionDeviceName, chargeThreshold))
                .setLocalOnly(true)
                .build()

        notificationManager.notify(BATTERY_CHARGED_NOTI_ID, noti)
        sharedPreferences.edit().putBoolean(PreferenceKey.BATTERY_CHARGED_NOTI_SENT, true).apply()
    }

    private fun cancelChargedNoti() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(BATTERY_CHARGED_NOTI_ID)
    }

    companion object {

        const val BATTERY_CHARGED_NOTI_CHANNEL_ID = "companion_device_charged"
        const val BATTERY_CHARGED_NOTI_ID = 408565
    }
}
