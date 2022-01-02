package com.boswelja.smartwatchextensions.batterysync

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import com.boswelja.smartwatchextensions.devicemanagement.WatchRepository
import com.boswelja.smartwatchextensions.messages.Message
import com.boswelja.smartwatchextensions.messages.MessagesRepository
import com.boswelja.smartwatchextensions.settings.BoolSettingKeys.BATTERY_STATS_NOTIFICATION_SENT
import com.boswelja.smartwatchextensions.settings.BoolSettingKeys.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.smartwatchextensions.settings.BoolSettingKeys.BATTERY_WATCH_CHARGE_NOTI_KEY
import com.boswelja.smartwatchextensions.settings.BoolSettingKeys.BATTERY_WATCH_LOW_NOTI_KEY
import com.boswelja.smartwatchextensions.settings.IntSettingKeys.BATTERY_CHARGE_THRESHOLD_KEY
import com.boswelja.smartwatchextensions.settings.IntSettingKeys.BATTERY_LOW_THRESHOLD_KEY
import com.boswelja.smartwatchextensions.settings.WatchSettingsRepository
import com.boswelja.watchconnection.common.Watch
import com.boswelja.watchconnection.common.message.ReceivedMessage
import com.boswelja.watchconnection.serialization.MessageReceiver
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * A [MessageReceiver] to receive [BatteryStats] and update [BatteryStatsDbRepository] with the new
 * data.
 */
class BatteryStatsReceiver :
    MessageReceiver<BatteryStats>(BatteryStatsSerializer),
    KoinComponent {

    private val batteryStatsRepository: BatteryStatsRepository by inject()
    private val messagesRepository: MessagesRepository by inject()
    private val watchRepository: WatchRepository by inject()
    private val settingsRepository: WatchSettingsRepository by inject()

    private lateinit var notificationManager: NotificationManager

    override suspend fun onMessageReceived(
        context: Context,
        message: ReceivedMessage<BatteryStats>
    ) {
        batteryStatsRepository.updateStatsFor(
            message.sourceUid,
            message.data
        )
        notificationManager = context.getSystemService()!!
        onBatteryStatsReceived(context, message.sourceUid, message.data)
    }

    /**
     * Called after battery stats have been saved into the repository.
     * @param context [Context].
     * @param sourceUid The source UID that sent the battery stats.
     * @param batteryStats The [BatteryStats] object.
     */
    private suspend fun onBatteryStatsReceived(
        context: Context,
        sourceUid: String,
        batteryStats: BatteryStats
    ) {
        val batterySyncEnabled = settingsRepository.getBoolean(sourceUid, BATTERY_SYNC_ENABLED_KEY, false).first()
        check(batterySyncEnabled) { "Received Battery Sync update while sync is disabled!" }
        val watch = watchRepository.getWatchById(sourceUid).first()
        checkNotNull(watch) { "Received battery stats for a watch that isn't registered!" }

        val chargeNotificationsEnabled = settingsRepository
            .getBoolean(sourceUid, BATTERY_WATCH_CHARGE_NOTI_KEY, false).first()
        val lowNotificationsEnabled = settingsRepository
            .getBoolean(sourceUid, BATTERY_WATCH_LOW_NOTI_KEY, false).first()

        if (batteryStats.charging && chargeNotificationsEnabled) {
            handleWatchChargeNoti(
                context,
                batteryStats,
                watch
            )
        } else if (!batteryStats.charging && lowNotificationsEnabled) {
            handleWatchLowNoti(
                context,
                batteryStats,
                watch
            )
        } else {
            cancelNotificationFor(sourceUid)
        }
    }

    /**
     * Checks if we can send the watch charge notification, and either send or cancel it
     * appropriately.
     * @param batteryStats The [BatteryStats] to send a notification for.
     * @param watch The [Watch] to send a notification for.
     */
    private suspend fun handleWatchChargeNoti(
        context: Context,
        batteryStats: BatteryStats,
        watch: Watch
    ) {
        val chargeThreshold = settingsRepository
            .getInt(watch.uid, BATTERY_CHARGE_THRESHOLD_KEY, BATTERY_CHARGE_DEFAULT)
            .first()
        val shouldSendChargeNotis = settingsRepository
            .getBoolean(watch.uid, BATTERY_WATCH_CHARGE_NOTI_KEY, false)
            .first()
        val hasSentNoti = settingsRepository
            .getBoolean(watch.uid, BATTERY_STATS_NOTIFICATION_SENT, false)
            .first()

        val shouldNotify = batteryStats.shouldPostChargeNotification(
            chargeThreshold,
            shouldSendChargeNotis,
            hasSentNoti
        )
        if (shouldNotify) {
            createNotificationChannel(context)
            if (areNotificationsEnabled(context)) {
                val notification = NotificationCompat.Builder(context, BATTERY_STATS_NOTI_CHANNEL_ID)
                    .setSmallIcon(R.drawable.battery_full)
                    .setContentTitle(
                        context.getString(R.string.device_battery_charged_noti_title, watch.name)
                    )
                    .setContentText(
                        context.getString(
                            R.string.device_battery_charged_noti_desc,
                            watch.name,
                            chargeThreshold.toString()
                        )
                    )
                    .setContentIntent(getNotiPendingIntent(context))
                    .setLocalOnly(true)
                    .build()
                postNotificationFor(watch.uid, notification)
            } else {
                messagesRepository.insert(
                    Message(
                        Message.Icon.ERROR,
                        context.getString(R.string.battery_charge_noti_issue_title),
                        context.getString(R.string.battery_charge_noti_issue_summary),
                        Message.Action.NOTIFICATION_SETTINGS,
                        System.currentTimeMillis()
                    ),
                    null
                )
            }
        }
    }

    /**
     * Checks if we can send the watch low notification, and either send or cancel it appropriately.
     * @param batteryStats The [BatteryStats] to send a notification for.
     * @param watch The [Watch] to send a notification for.
     */
    private suspend fun handleWatchLowNoti(
        context: Context,
        batteryStats: BatteryStats,
        watch: Watch
    ) {
        val lowThreshold = settingsRepository
            .getInt(watch.uid, BATTERY_LOW_THRESHOLD_KEY, BATTERY_LOW_DEFAULT)
            .first()
        val shouldSendLowNoti = settingsRepository
            .getBoolean(watch.uid, BATTERY_WATCH_LOW_NOTI_KEY, false)
            .first()
        val hasSentNoti = settingsRepository
            .getBoolean(watch.uid, BATTERY_STATS_NOTIFICATION_SENT, false)
            .first()

        // We can send a low noti if the user has enabled them, we haven't already sent it and
        // the watch is sufficiently discharged.
        val shouldNotify = batteryStats.shouldPostLowNotification(
            lowThreshold,
            shouldSendLowNoti,
            hasSentNoti
        )
        if (shouldNotify) {
            createNotificationChannel(context)
            if (areNotificationsEnabled(context)) {
                val notification = NotificationCompat.Builder(context, BATTERY_STATS_NOTI_CHANNEL_ID)
                    .setSmallIcon(R.drawable.battery_alert)
                    .setContentTitle(
                        context.getString(R.string.device_battery_low_noti_title, watch.name)
                    )
                    .setContentIntent(getNotiPendingIntent(context))
                    .setContentText(
                        context.getString(R.string.device_battery_low_noti_desc, watch.name, lowThreshold.toString())
                    )
                    .setLocalOnly(true)
                    .build()
                postNotificationFor(watch.uid, notification)
            } else {
                messagesRepository.insert(
                    Message(
                        Message.Icon.ERROR,
                        context.getString(R.string.battery_low_noti_issue_title),
                        context.getString(R.string.battery_low_noti_issue_summary),
                        Message.Action.NOTIFICATION_SETTINGS,
                        System.currentTimeMillis()
                    ),
                    null
                )
            }
        }
    }

    private fun getNotiPendingIntent(context: Context): PendingIntent {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        return PendingIntent.getActivity(
            context,
            START_ACTIVITY_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Checks whether notifications are enabled for the required channel.
     * @return true if notifications are enabled, false otherwise.
     */
    private fun areNotificationsEnabled(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.getNotificationChannel(BATTERY_STATS_NOTI_CHANNEL_ID).let {
                it != null && it.importance != NotificationManager.IMPORTANCE_NONE
            }
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    /**
     * Create a notification channel for battery stats notifications.
     */
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(BATTERY_STATS_NOTI_CHANNEL_ID) == null) {
                NotificationChannel(
                    BATTERY_STATS_NOTI_CHANNEL_ID,
                    context.getString(R.string.noti_channel_watch_charged_title),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    enableLights(false)
                    enableVibration(true)
                    setShowBadge(true)
                }.also { notificationManager.createNotificationChannel(it) }
            }
        }
    }

    private suspend fun postNotificationFor(watchUid: String, notification: Notification) {
        notificationManager.notify(calculateNotificationId(watchUid), notification)
        settingsRepository.putBoolean(watchUid, BATTERY_STATS_NOTIFICATION_SENT, true)
    }

    private suspend fun cancelNotificationFor(watchUid: String) {
        notificationManager.cancel(calculateNotificationId(watchUid))
        settingsRepository.putBoolean(watchUid, BATTERY_STATS_NOTIFICATION_SENT, false)
    }

    private fun calculateNotificationId(watchUid: String): Int = watchUid.hashCode()

    companion object {
        /**
         * A notification channel ID to use for battery stats
         */
        private const val BATTERY_STATS_NOTI_CHANNEL_ID = "companion_device_charged"

        private const val START_ACTIVITY_REQUEST_CODE = 123

        private const val BATTERY_CHARGE_DEFAULT = 90
        private const val BATTERY_LOW_DEFAULT = 20
    }
}
