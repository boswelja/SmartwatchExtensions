package com.boswelja.smartwatchextensions.batterysync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import com.boswelja.smartwatchextensions.batterysync.Utils.BATTERY_STATS_NOTI_CHANNEL_ID
import com.boswelja.smartwatchextensions.devicemanagement.WatchRepository
import com.boswelja.smartwatchextensions.messages.Message
import com.boswelja.smartwatchextensions.messages.MessagesRepository
import com.boswelja.smartwatchextensions.settings.BoolSettingKeys.BATTERY_CHARGED_NOTI_SENT
import com.boswelja.smartwatchextensions.settings.BoolSettingKeys.BATTERY_LOW_NOTI_SENT
import com.boswelja.smartwatchextensions.settings.BoolSettingKeys.BATTERY_WATCH_CHARGE_NOTI_KEY
import com.boswelja.smartwatchextensions.settings.BoolSettingKeys.BATTERY_WATCH_LOW_NOTI_KEY
import com.boswelja.smartwatchextensions.settings.IntSettingKeys.BATTERY_CHARGE_THRESHOLD_KEY
import com.boswelja.smartwatchextensions.settings.IntSettingKeys.BATTERY_LOW_THRESHOLD_KEY
import com.boswelja.smartwatchextensions.settings.WatchSettingsRepository
import com.boswelja.watchconnection.common.Watch
import com.boswelja.watchconnection.common.message.ReceivedMessage
import com.boswelja.watchconnection.serialization.MessageReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Locale

private const val BATTERY_CHARGED_NOTI_ID = 408565
private const val BATTERY_LOW_NOTI_ID = 408566
private const val START_ACTIVITY_REQUEST_CODE = 123

private const val BATTERY_CHARGE_DEFAULT = 90
private const val BATTERY_LOW_DEFAULT = 20

/**
 * A [MessageReceiver] to receive [BatteryStats] and update [BatteryStatsDbRepository] with the new
 * data.
 */
class BatteryStatsReceiver :
    MessageReceiver<BatteryStats?>(BatteryStatsSerializer),
    KoinComponent {

    private val batteryStatsRepository: BatteryStatsRepository by inject()
    private val messagesRepository: MessagesRepository by inject()
    private val watchRepository: WatchRepository by inject()
    private val settingsRepository: WatchSettingsRepository by inject()

    override suspend fun onMessageReceived(
        context: Context,
        message: ReceivedMessage<BatteryStats?>
    ) {
        message.data?.let { batteryStats ->
            batteryStatsRepository.updateStatsFor(
                message.sourceUid,
                batteryStats
            )
            onBatteryStatsReceived(context, message.sourceUid, batteryStats)
        }
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
        // TODO This can be optimised
        withContext(Dispatchers.IO) {
            watchRepository.getWatchById(sourceUid).firstOrNull()?.let { watch ->
                val notificationManager = context.getSystemService<NotificationManager>()!!
                if (batteryStats.charging) {
                    dismissLowNoti(
                        notificationManager,
                        watch
                    )
                    handleWatchChargeNoti(
                        context,
                        notificationManager,
                        batteryStats,
                        watch
                    )
                } else {
                    dismissChargeNoti(
                        notificationManager,
                        watch
                    )
                    handleWatchLowNoti(
                        context,
                        notificationManager,
                        batteryStats,
                        watch
                    )
                }
            }

            // TODO Restore this
//            // Update battery stat widgets
//            WatchWidgetProvider.updateWidgets(context)
//
//            // Update QS Tile
//            WatchBatteryTileService.requestTileUpdate(context)
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
        notificationManager: NotificationManager,
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
            .getBoolean(watch.uid, BATTERY_CHARGED_NOTI_SENT, false)
            .first()

        val shouldNotify = batteryStats.shouldPostChargeNotification(
            chargeThreshold,
            shouldSendChargeNotis,
            hasSentNoti
        )
        if (shouldNotify) {
            createNotificationChannel(context, notificationManager)
            if (areNotificationsEnabled(context)) {
                NotificationCompat.Builder(
                    context,
                    BATTERY_STATS_NOTI_CHANNEL_ID
                )
                    .setSmallIcon(R.drawable.battery_full)
                    .setContentTitle(
                        context.getString(R.string.device_battery_charged_noti_title, watch.name)
                    )
                    .setContentText(
                        context.getString(R.string.device_battery_charged_noti_desc)
                            .format(Locale.getDefault(), watch.name, chargeThreshold)
                    )
                    .setContentIntent(getNotiPendingIntent(context))
                    .setLocalOnly(true)
                    .also { notificationManager.notify(BATTERY_CHARGED_NOTI_ID, it.build()) }
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
            settingsRepository.putBoolean(watch.uid, BATTERY_CHARGED_NOTI_SENT, true)
        }
    }

    /**
     * Checks if we can send the watch low notification, and either send or cancel it appropriately.
     * @param batteryStats The [BatteryStats] to send a notification for.
     * @param watch The [Watch] to send a notification for.
     */
    private suspend fun handleWatchLowNoti(
        context: Context,
        notificationManager: NotificationManager,
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
            .getBoolean(watch.uid, BATTERY_LOW_NOTI_SENT, false)
            .first()

        // We can send a low noti if the user has enabled them, we haven't already sent it and
        // the watch is sufficiently discharged.
        val shouldNotify = batteryStats.shouldPostLowNotification(
            lowThreshold,
            shouldSendLowNoti,
            hasSentNoti
        )
        if (shouldNotify) {
            createNotificationChannel(context, notificationManager)
            if (areNotificationsEnabled(context)) {
                NotificationCompat.Builder(
                    context,
                    BATTERY_STATS_NOTI_CHANNEL_ID
                )
                    .setSmallIcon(R.drawable.battery_alert)
                    .setContentTitle(
                        context.getString(R.string.device_battery_low_noti_title, watch.name)
                    )
                    .setContentIntent(getNotiPendingIntent(context))
                    .setContentText(
                        context.getString(R.string.device_battery_low_noti_desc)
                            .format(Locale.getDefault(), watch.name, lowThreshold)
                    )
                    .setLocalOnly(true)
                    .also { notificationManager.notify(BATTERY_LOW_NOTI_ID, it.build()) }
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
            settingsRepository.putBoolean(watch.uid, BATTERY_LOW_NOTI_SENT, true)
        }
    }

    /**
     * Dismiss a device charge notification for the given watch if it exists.
     * @param notificationManager [NotificationManager].
     * @param watch The watch whose charge notification should be dismissed.
     */
    private suspend fun dismissChargeNoti(
        notificationManager: NotificationManager,
        watch: Watch
    ) {
        notificationManager.cancel(BATTERY_CHARGED_NOTI_ID)
        settingsRepository.putBoolean(watch.uid, BATTERY_CHARGED_NOTI_SENT, false)
    }

    /**
     * Dismiss a device low notification for the given watch if it exists.
     * @param notificationManager [NotificationManager].
     * @param watch The watch whose low notification should be dismissed.
     */
    private suspend fun dismissLowNoti(
        notificationManager: NotificationManager,
        watch: Watch
    ) {
        notificationManager.cancel(BATTERY_LOW_NOTI_ID)
        settingsRepository.putBoolean(watch.uid, BATTERY_LOW_NOTI_SENT, false)
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
        val notificationManager = context.getSystemService<NotificationManager>()!!
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
    private fun createNotificationChannel(context: Context, notificationManager: NotificationManager) {
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
}
