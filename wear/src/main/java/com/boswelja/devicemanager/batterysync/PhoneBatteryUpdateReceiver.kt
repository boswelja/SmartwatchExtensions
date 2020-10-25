/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.batterysync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import androidx.core.content.getSystemService
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.References.CAPABILITY_PHONE_APP
import com.boswelja.devicemanager.common.batterysync.BatteryStats
import com.boswelja.devicemanager.common.batterysync.References.BATTERY_STATUS_PATH
import com.boswelja.devicemanager.common.batterysync.Utils.updateBatteryStats
import com.boswelja.devicemanager.common.preference.PreferenceKey
import com.boswelja.devicemanager.phoneconnectionmanager.References.PHONE_NAME_KEY
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import timber.log.Timber

class PhoneBatteryUpdateReceiver : WearableListenerService() {

    private val sharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(this) }

    override fun onMessageReceived(messageEvent: MessageEvent?) {
        if (messageEvent?.path == BATTERY_STATUS_PATH) {
            Timber.i("Got battery stats from ${messageEvent.sourceNodeId}")

            val batteryStats = BatteryStats.fromMessage(messageEvent)
            sharedPreferences.edit {
                putInt(PreferenceKey.BATTERY_PERCENT_KEY, batteryStats.percent)
            }

            handleChargeNotification(batteryStats)

            updateBatteryStats(this, CAPABILITY_PHONE_APP)
            PhoneBatteryComplicationProvider.updateAll(this)
        }
    }

    /**
     * Decides whether a device charged notification should be sent to the user, and either sends
     * the notification or cancels any existing notifications accordingly.
     * @param batteryStats The [BatteryStats] object to read data from.
     */
    private fun handleChargeNotification(batteryStats: BatteryStats) {
        Timber.d("handleChargeNotification($batteryStats) called")
        val shouldNotifyUser =
            sharedPreferences.getBoolean(PreferenceKey.BATTERY_PHONE_CHARGE_NOTI_KEY, false)
        if (batteryStats.isCharging && shouldNotifyUser) {
            val chargeThreshold =
                sharedPreferences.getInt(PreferenceKey.BATTERY_CHARGE_THRESHOLD_KEY, 90)
            val hasNotiBeenSent =
                sharedPreferences.getBoolean(PreferenceKey.BATTERY_CHARGED_NOTI_SENT, false)

            if (batteryStats.percent >= chargeThreshold && !hasNotiBeenSent) {
                val phoneName =
                    sharedPreferences.getString(
                        PHONE_NAME_KEY, getString(R.string.default_phone_name))
                        ?: getString(R.string.default_phone_name)
                notifyCharged(phoneName, chargeThreshold)
            }
        } else {
            Timber.i("Cancelling any existing charge notifications")
            getSystemService<NotificationManager>()?.cancel(BATTERY_CHARGED_NOTI_ID)
            sharedPreferences.edit { putBoolean(PreferenceKey.BATTERY_CHARGED_NOTI_SENT, false) }
        }
    }

    /**
     * Creates and sends the device charged [NotificationCompat]. This will also create the required
     * [NotificationChannel] if necessary.
     * @param deviceName The name of the device that's charged.
     * @param chargeThreshold The minimum charge percent required to send the device charged
     * notification.
     */
    private fun notifyCharged(deviceName: String, chargeThreshold: Int) {
        Timber.d("notifyCharged($deviceName, $chargeThreshold) called")
        getSystemService<NotificationManager>()?.let { notificationManager ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                notificationManager.getNotificationChannel(BATTERY_CHARGED_NOTI_CHANNEL_ID) ==
                    null) {
                Timber.i("Creating notification channel")
                val channel =
                    NotificationChannel(
                            BATTERY_CHARGED_NOTI_CHANNEL_ID,
                            getString(R.string.noti_channel_phone_charged_title),
                            NotificationManager.IMPORTANCE_HIGH)
                        .apply {
                            enableVibration(true)
                            setShowBadge(true)
                        }
                notificationManager.createNotificationChannel(channel)
            }

            val noti =
                NotificationCompat.Builder(this, BATTERY_CHARGED_NOTI_CHANNEL_ID)
                    .setSmallIcon(R.drawable.battery_full)
                    .setContentTitle(getString(R.string.device_charged_noti_title, deviceName))
                    .setContentText(
                        getString(
                            R.string.device_charged_noti_desc,
                            deviceName,
                            chargeThreshold.toString()))
                    .setLocalOnly(true)
                    .build()

            notificationManager.notify(BATTERY_CHARGED_NOTI_ID, noti)
            sharedPreferences.edit { putBoolean(PreferenceKey.BATTERY_CHARGED_NOTI_SENT, true) }
            Timber.i("Notification sent")
        }
    }

    companion object {
        private const val BATTERY_CHARGED_NOTI_ID = 408565
        private const val BATTERY_CHARGED_NOTI_CHANNEL_ID = "companion_device_charged"
    }
}
