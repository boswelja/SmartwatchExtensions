package com.boswelja.devicemanager.batterysync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.batterysync.BatteryStats
import com.boswelja.devicemanager.common.batterysync.References.BATTERY_STATUS_PATH
import com.boswelja.devicemanager.extensions.extensionSettingsStore
import com.boswelja.devicemanager.phoneStateStore
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import timber.log.Timber

class PhoneBatteryUpdateReceiver : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent?) {
        // Execution here runs on a separate background thread.
        // See https://developer.android.com/training/wearables/data-layer/events#sync-waiting
        if (messageEvent?.path == BATTERY_STATUS_PATH) {
            Timber.i("Got battery stats from ${messageEvent.sourceNodeId}")

            val batteryStats = BatteryStats.fromByteArray(messageEvent.data)
            runBlocking {
                phoneStateStore.updateData {
                    it.copy(batteryPercent = batteryStats.percent)
                }
                handleChargeNotification(batteryStats)
            }

            updateBatteryStats(this, messageEvent.sourceNodeId)
            PhoneBatteryComplicationProvider.updateAll(this)
        }
    }

    /**
     * Decides whether a device charged notification should be sent to the user, and either sends
     * the notification or cancels any existing notifications accordingly.
     * @param batteryStats The [BatteryStats] object to read data from.
     */
    private suspend fun handleChargeNotification(batteryStats: BatteryStats) {
        Timber.d("handleChargeNotification($batteryStats) called")
        val shouldNotifyUser = extensionSettingsStore.data.map { it.phoneChargeNotiEnabled }.first()
        if (batteryStats.isCharging && shouldNotifyUser) {
            val chargeThreshold = extensionSettingsStore.data
                .map { it.batteryChargeThreshold }.first()
            val hasNotiBeenSent = phoneStateStore.data.map { it.chargeNotiSent }.first()

            if (batteryStats.percent >= chargeThreshold && !hasNotiBeenSent) {
                val phoneName = phoneStateStore.data.map { it.name }.first()
                notifyCharged(phoneName, chargeThreshold)
            }
        } else {
            Timber.i("Cancelling any existing charge notifications")
            getSystemService<NotificationManager>()?.cancel(BATTERY_CHARGED_NOTI_ID)
            phoneStateStore.updateData {
                it.copy(chargeNotiSent = false)
            }
        }
    }

    /**
     * Creates and sends the device charged [NotificationCompat]. This will also create the required
     * [NotificationChannel] if necessary.
     * @param deviceName The name of the device that's charged.
     * @param chargeThreshold The minimum charge percent required to send the device charged
     * notification.
     */
    private suspend fun notifyCharged(deviceName: String, chargeThreshold: Int) {
        Timber.d("notifyCharged($deviceName, $chargeThreshold) called")
        getSystemService<NotificationManager>()?.let { notificationManager ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                notificationManager.getNotificationChannel(BATTERY_CHARGED_NOTI_CHANNEL_ID) ==
                null
            ) {
                Timber.i("Creating notification channel")
                val channel =
                    NotificationChannel(
                        BATTERY_CHARGED_NOTI_CHANNEL_ID,
                        getString(R.string.noti_channel_phone_charged_title),
                        NotificationManager.IMPORTANCE_HIGH
                    )
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
                            chargeThreshold.toString()
                        )
                    )
                    .setLocalOnly(true)
                    .build()

            notificationManager.notify(BATTERY_CHARGED_NOTI_ID, noti)
            phoneStateStore.updateData {
                it.copy(chargeNotiSent = true)
            }
            Timber.i("Notification sent")
        }
    }

    /** Sends a battery status update to connected devices. */
    private fun updateBatteryStats(context: Context, phoneId: String) {
        val batteryStats = BatteryStats.createForDevice(context)
        if (batteryStats != null) {
            Wearable.getMessageClient(context).sendMessage(
                phoneId, BATTERY_STATUS_PATH, batteryStats.toByteArray()
            )
        } else {
            Timber.w("batteryStats null, skipping...")
        }
    }

    companion object {
        private const val BATTERY_CHARGED_NOTI_ID = 408565
        private const val BATTERY_CHARGED_NOTI_CHANNEL_ID = "companion_device_charged"
    }
}
