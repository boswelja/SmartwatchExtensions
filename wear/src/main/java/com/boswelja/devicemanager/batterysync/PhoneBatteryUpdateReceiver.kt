/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.batterysync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.support.wearable.complications.ProviderUpdateRequester
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.common.References
import com.boswelja.devicemanager.common.batterysync.Utils.updateBatteryStats
import com.boswelja.devicemanager.complication.PhoneBatteryComplicationProvider
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService

class PhoneBatteryUpdateReceiver : WearableListenerService() {

    lateinit var sharedPreferences: SharedPreferences

    override fun onMessageReceived(messageEvent: MessageEvent?) {
        if (messageEvent?.path == com.boswelja.devicemanager.common.batterysync.References.BATTERY_STATUS_PATH) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            val message = String(messageEvent.data, Charsets.UTF_8)
            val messageSplit = message.split("|")
            val percent = messageSplit[0].toInt()
            val charging = messageSplit[1] == true.toString()
            sharedPreferences.edit {
                putInt(PreferenceKey.BATTERY_PERCENT_KEY, percent)
            }

            val shouldNotifyDeviceCharged = sharedPreferences.getBoolean(PreferenceKey.BATTERY_PHONE_CHARGE_NOTI_KEY, false)
            if (charging && shouldNotifyDeviceCharged) {
                val chargeThreshold = sharedPreferences.getInt(PreferenceKey.BATTERY_CHARGE_THRESHOLD_KEY, 90)
                if (percent >= chargeThreshold && !sharedPreferences.getBoolean(PreferenceKey.BATTERY_CHARGED_NOTI_SENT, false)) {
                    notifyCharged(Tasks.await(Wearable.getNodeClient(this).connectedNodes).firstOrNull { it.id == messageEvent.sourceNodeId }?.displayName, chargeThreshold)
                }
            }

            if (!charging && sharedPreferences.getBoolean(PreferenceKey.BATTERY_CHARGED_NOTI_SENT, false)) {
                sharedPreferences.edit().putBoolean(PreferenceKey.BATTERY_CHARGED_NOTI_SENT, false).apply()
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(BATTERY_CHARGED_NOTI_ID)
            }

            updateBatteryStats(this, References.CAPABILITY_PHONE_APP)
            ProviderUpdateRequester(this, ComponentName(packageName, PhoneBatteryComplicationProvider::class.java.name)).requestUpdateAll()
        }
    }

    private fun notifyCharged(deviceName: String?, chargeThreshold: Int) {
        if (!deviceName.isNullOrEmpty()) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationManager.getNotificationChannel(BATTERY_CHARGED_NOTI_CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                        BATTERY_CHARGED_NOTI_CHANNEL_ID,
                        getString(R.string.noti_channel_phone_charged_title),
                        NotificationManager.IMPORTANCE_HIGH).apply {
                    enableVibration(true)
                    setShowBadge(true)
                }
                notificationManager.createNotificationChannel(channel)
            }

            val noti = NotificationCompat.Builder(this, BATTERY_CHARGED_NOTI_CHANNEL_ID)
                    .setSmallIcon(R.drawable.battery_full)
                    .setContentTitle(getString(R.string.device_charged_noti_title, deviceName))
                    .setContentText(getString(R.string.device_charged_noti_desc).format(deviceName, chargeThreshold))
                    .setLocalOnly(true)
                    .build()

            notificationManager.notify(BATTERY_CHARGED_NOTI_ID, noti)
            sharedPreferences.edit().putBoolean(PreferenceKey.BATTERY_CHARGED_NOTI_SENT, true).apply()
        }
    }

    companion object {

        const val BATTERY_CHARGED_NOTI_CHANNEL_ID = "companion_device_charged"
        const val BATTERY_CHARGED_NOTI_ID = 408565
    }
}
