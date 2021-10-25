package com.boswelja.smartwatchextensions.batterysync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.datastore.core.DataStore
import com.boswelja.smartwatchextensions.PhoneState
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.extensions.ExtensionSettings
import com.boswelja.smartwatchextensions.extensions.extensionSettingsStore
import com.boswelja.smartwatchextensions.main.ui.MainActivity
import com.boswelja.smartwatchextensions.phoneStateStore
import com.boswelja.watchconection.common.message.MessageReceiver
import com.boswelja.watchconnection.common.message.Message
import com.boswelja.watchconnection.common.message.ReceivedMessage
import com.boswelja.watchconnection.wear.discovery.DiscoveryClient
import com.boswelja.watchconnection.wear.message.MessageClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

/**
 * A [MessageReceiver] for receiving [BatteryStats] for the paired phone.
 */
class PhoneBatteryUpdateReceiver :
    MessageReceiver<BatteryStats?>(BatteryStatsSerializer),
    KoinComponent {

    private val messageClient: MessageClient by inject()
    private val discoveryClient: DiscoveryClient by inject()

    private lateinit var notificationManager: NotificationManager
    private lateinit var phoneStateStore: DataStore<PhoneState>
    private lateinit var extensionSettingsStore: DataStore<ExtensionSettings>

    override suspend fun onMessageReceived(
        context: Context,
        message: ReceivedMessage<BatteryStats?>
    ) {
        message.data?.let { batteryStats ->
            notificationManager = context.getSystemService()!!
            phoneStateStore = context.phoneStateStore
            extensionSettingsStore = context.extensionSettingsStore

            if (batteryStats.charging) {
                cancelLowNoti()
                handleChargeNotification(context, batteryStats)
            } else {
                cancelChargeNoti()
                handleLowNotification(context, batteryStats)
            }
            BatteryStatsStore(context.batteryStatsStore).updateStatsForPhone(batteryStats)
            sendBatteryStatsUpdate(context)
            PhoneBatteryComplicationProvider.updateAll(context)
        }
    }

    /**
     * Decides whether a device charged notification should be sent to the user, and either sends
     * the notification or cancels any existing notifications accordingly.
     * @param batteryStats The [BatteryStats] object to read data from.
     */
    private suspend fun handleChargeNotification(context: Context, batteryStats: BatteryStats) {
        val notificationsEnabled = extensionSettingsStore.data
            .map { it.phoneChargeNotiEnabled }.first()
        val chargeThreshold = extensionSettingsStore.data
            .map { it.batteryChargeThreshold }.first()
        val hasNotiBeenSent = phoneStateStore.data.map { it.chargeNotiSent }.first()

        val shouldNotify = batteryStats.shouldPostChargeNotification(
            chargeThreshold,
            notificationsEnabled,
            hasNotiBeenSent
        )
        if (shouldNotify) {
            val phoneName = phoneStateStore.data.map { it.name }.first()
            notifyBatteryCharged(context, phoneName, chargeThreshold)
        }
    }

    private suspend fun cancelChargeNoti() {
        Timber.i("Cancelling any existing charge notifications")
        notificationManager.cancel(BATTERY_CHARGED_NOTI_ID)
        phoneStateStore.updateData {
            it.copy(chargeNotiSent = false)
        }
    }

    private suspend fun cancelLowNoti() {
        Timber.i("Cancelling any existing low notifications")
        notificationManager.cancel(BATTERY_LOW_NOTI_ID)
        phoneStateStore.updateData {
            it.copy(lowNotiSent = false)
        }
    }

    /**
     * Decides whether a device charged notification should be sent to the user, and either sends
     * the notification or cancels any existing notifications accordingly.
     * @param batteryStats The [BatteryStats] object to read data from.
     */
    private suspend fun handleLowNotification(
        context: Context,
        batteryStats: BatteryStats
    ) {
        Timber.d("handleLowNotification($batteryStats) called")
        val notificationsEnabled = extensionSettingsStore.data
            .map { it.phoneLowNotiEnabled }.first()
        val lowThreshold = extensionSettingsStore.data.map { it.batteryLowThreshold }.first()
        val hasNotiBeenSent = phoneStateStore.data.map { it.lowNotiSent }.first()

        val shouldNotify = batteryStats.shouldPostLowNotification(
            lowThreshold,
            notificationsEnabled,
            hasNotiBeenSent
        )
        if (shouldNotify) {
            val phoneName = phoneStateStore.data.map { it.name }.first()
            notifyBatteryLow(context, phoneName, lowThreshold)
        }
    }

    /**
     * Creates and sends the device charged [NotificationCompat]. This will also create the required
     * [NotificationChannel] if necessary.
     * @param deviceName The name of the device that's charged.
     * @param chargeThreshold The minimum charge percent required to send the device charged
     * notification.
     */
    private suspend fun notifyBatteryLow(
        context: Context,
        deviceName: String,
        chargeThreshold: Int
    ) {
        Timber.d("notifyCharged($deviceName, $chargeThreshold) called")
        createNotificationChannel(context)

        val noti =
            NotificationCompat.Builder(context, BATTERY_STATS_NOTI_CHANNEL_ID)
                .setSmallIcon(R.drawable.battery_alert)
                .setContentTitle(
                    context.getString(R.string.device_battery_low_noti_title, deviceName)
                )
                .setContentText(
                    context.getString(
                        R.string.device_battery_low_noti_desc,
                        deviceName,
                        chargeThreshold.toString()
                    )
                )
                .setContentIntent(getNotiPendingIntent(context))
                .setLocalOnly(true)
                .build()

        notificationManager.notify(BATTERY_LOW_NOTI_ID, noti)
        phoneStateStore.updateData {
            it.copy(lowNotiSent = true)
        }
        Timber.i("Notification sent")
    }

    /**
     * Creates and sends the device charged [NotificationCompat]. This will also create the required
     * [NotificationChannel] if necessary.
     * @param deviceName The name of the device that's charged.
     * @param chargeThreshold The minimum charge percent required to send the device charged
     * notification.
     */
    private suspend fun notifyBatteryCharged(
        context: Context,
        deviceName: String,
        chargeThreshold: Int
    ) {
        Timber.d("notifyCharged($deviceName, $chargeThreshold) called")
        createNotificationChannel(context)

        val noti =
            NotificationCompat.Builder(context, BATTERY_STATS_NOTI_CHANNEL_ID)
                .setSmallIcon(R.drawable.battery_full)
                .setContentTitle(
                    context.getString(R.string.device_battery_charged_noti_title, deviceName)
                )
                .setContentText(
                    context.getString(
                        R.string.device_battery_charged_noti_desc,
                        deviceName,
                        chargeThreshold.toString()
                    )
                )
                .setContentIntent(getNotiPendingIntent(context))
                .setLocalOnly(true)
                .build()

        notificationManager.notify(BATTERY_CHARGED_NOTI_ID, noti)
        phoneStateStore.updateData {
            it.copy(chargeNotiSent = true)
        }
        Timber.i("Notification sent")
    }

    /** Sends a battery status update to connected devices. */
    private suspend fun sendBatteryStatsUpdate(context: Context) {
        val batteryStats = context.batteryStats()
        if (batteryStats != null) {
            messageClient.sendMessage(
                discoveryClient.pairedPhone()!!,
                Message(BATTERY_STATUS_PATH, batteryStats)
            )
        } else {
            Timber.w("batteryStats null, skipping...")
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (notificationManager.getNotificationChannel(BATTERY_STATS_NOTI_CHANNEL_ID) == null) {
            Timber.i("Creating notification channel")
            val channel =
                NotificationChannel(
                    BATTERY_STATS_NOTI_CHANNEL_ID,
                    context.getString(R.string.noti_channel_battery_stats_title),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    enableVibration(true)
                    setShowBadge(true)
                }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun getNotiPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        return PendingIntent.getActivity(
            context,
            START_ACTIVITY_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        private const val START_ACTIVITY_REQUEST_CODE = 123
        private const val BATTERY_CHARGED_NOTI_ID = 408565
        private const val BATTERY_LOW_NOTI_ID = 408566
        private const val BATTERY_STATS_NOTI_CHANNEL_ID = "companion_device_charged"
    }
}
