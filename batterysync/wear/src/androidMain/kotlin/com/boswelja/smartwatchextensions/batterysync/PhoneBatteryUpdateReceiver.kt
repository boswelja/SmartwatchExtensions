package com.boswelja.smartwatchextensions.batterysync

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.datastore.core.DataStore
import com.boswelja.smartwatchextensions.devicemanagement.PhoneState
import com.boswelja.smartwatchextensions.devicemanagement.phoneStateStore
import com.boswelja.watchconnection.common.message.Message
import com.boswelja.watchconnection.common.message.ReceivedMessage
import com.boswelja.watchconnection.serialization.MessageHandler
import com.boswelja.watchconnection.serialization.MessageReceiver
import com.boswelja.watchconnection.wear.message.MessageClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * A [MessageReceiver] for receiving [BatteryStats] for the paired phone.
 */
class PhoneBatteryUpdateReceiver :
    MessageReceiver<BatteryStats>(BatteryStatsSerializer),
    KoinComponent {

    private val messageClient: MessageClient by inject()
    private val batteryStatsRepository: BatteryStatsRepository by inject()
    private val batterySyncStateRepository: BatterySyncStateRepository by inject()

    private lateinit var notificationManager: NotificationManager
    private lateinit var phoneStateStore: DataStore<PhoneState>

    override suspend fun onMessageReceived(
        context: Context,
        message: ReceivedMessage<BatteryStats>
    ) {
        val batteryStats = message.data
        val batterySyncState = batterySyncStateRepository.getBatterySyncState().first()

        check(!batterySyncState.batterySyncEnabled) { "Received Battery Sync update while sync is disabled!" }

        // Store updated stats
        batteryStatsRepository.updatePhoneBatteryStats(batteryStats)
        sendBatteryStatsUpdate(context, message.sourceUid)
        PhoneBatteryComplicationProvider.updateAll(context)

        // Handle notifications
        notificationManager = context.getSystemService()!!
        phoneStateStore = context.phoneStateStore
        createNotificationChannel(context)
        if (batteryStats.charging && batterySyncState.phoneChargeNotificationEnabled) {
            tryPostChargeNotification(context, batteryStats, batterySyncState.phoneChargeThreshold)
        } else if (!batteryStats.charging && batterySyncState.phoneLowNotificationEnabled) {
            tryPostLowNotification(context, batteryStats, batterySyncState.phoneLowThreshold)
        } else {
            cancelNotification()
        }
    }

    /**
     * If all conditions are met, posts a notification informing the user their phone is charged.
     * @param context [Context].
     * @param batteryStats The [BatteryStats] sent from the phone.
     * @param chargeThreshold The battery percent threshold to consider the device charged at.
     */
    private suspend fun tryPostChargeNotification(
        context: Context,
        batteryStats: BatteryStats,
        chargeThreshold: Int
    ) {
        val shouldNotify = batteryStats.percent >= chargeThreshold
        if (shouldNotify) {
            val phoneName = phoneStateStore.data.map { it.name }.first()
            val notification = createBaseNotificationBuilder(context)
                .setSmallIcon(R.drawable.battery_full)
                .setContentTitle(
                    context.getString(R.string.device_battery_charged_noti_title, phoneName)
                )
                .setContentText(
                    context.getString(
                        R.string.device_battery_charged_noti_desc,
                        phoneName,
                        chargeThreshold.toString()
                    )
                )
                .build()
            postNotification(notification)
        }
    }

    /**
     * If all conditions are met, posts a notification informing the user their phone is low.
     * @param context [Context].
     * @param batteryStats The [BatteryStats] sent from the phone.
     * @param lowThreshold The battery percent threshold to consider the device low at.
     */
    private suspend fun tryPostLowNotification(
        context: Context,
        batteryStats: BatteryStats,
        lowThreshold: Int
    ) {
        val shouldNotify = batteryStats.percent <= lowThreshold
        if (shouldNotify) {
            val phoneName = phoneStateStore.data.map { it.name }.first()
            val notification = createBaseNotificationBuilder(context)
                .setSmallIcon(R.drawable.battery_alert)
                .setContentTitle(
                    context.getString(R.string.device_battery_low_noti_title, phoneName)
                )
                .setContentText(
                    context.getString(
                        R.string.device_battery_low_noti_desc,
                        phoneName,
                        lowThreshold.toString()
                    )
                )
                .build()
            postNotification(notification)
        }
    }

    /**
     * Post the given notification to the system.
     */
    private suspend fun postNotification(notification: Notification) {
        notificationManager.notify(BATTERY_NOTI_ID, notification)
        batterySyncStateRepository.updateBatterySyncState {
            it.copy(notificationPosted = true)
        }
    }

    /**
     * Cancel any notifications sent by Battery Sync.
     */
    private suspend fun cancelNotification() {
        notificationManager.cancel(BATTERY_NOTI_ID)
        batterySyncStateRepository.updateBatterySyncState {
            it.copy(notificationPosted = false)
        }
    }

    /**
     *  Get an up to date [BatteryStats] and send it to the given target.
     *  @param context [Context].
     *  @param targetUid The target device UID.
     */
    private suspend fun sendBatteryStatsUpdate(context: Context, targetUid: String) {
        val handler = MessageHandler(BatteryStatsSerializer, messageClient)
        val batteryStats = context.batteryStats()
        if (batteryStats != null) {
            handler.sendMessage(
                targetUid,
                Message(BATTERY_STATUS_PATH, batteryStats)
            )
        }
    }

    /**
     * If needed, creates a notification channel to post Battery Sync notifications to.
     * @param context [Context].
     */
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(BATTERY_STATS_NOTI_CHANNEL_ID) == null) {
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
    }

    /**
     * Create a base [NotificationCompat.Builder] for Battery Sync notifications. All posted
     * notifications should build from this for consistency.
     * @param context [Context].
     */
    private fun createBaseNotificationBuilder(context: Context): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, BATTERY_STATS_NOTI_CHANNEL_ID)
            .setContentIntent(createLaunchPendingIntent(context))
            .setLocalOnly(true)
            .setOnlyAlertOnce(true)
    }

    /**
     * Create a [PendingIntent] to launch the app.
     * @param context [Context].
     */
    private fun createLaunchPendingIntent(context: Context): PendingIntent {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        return PendingIntent.getActivity(
            context,
            START_ACTIVITY_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        private const val START_ACTIVITY_REQUEST_CODE = 123
        private const val BATTERY_NOTI_ID = 408565
        private const val BATTERY_STATS_NOTI_CHANNEL_ID = "companion_device_charged"
    }
}
