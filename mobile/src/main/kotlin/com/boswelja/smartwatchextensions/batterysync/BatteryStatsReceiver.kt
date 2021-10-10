package com.boswelja.smartwatchextensions.batterysync

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.boswelja.smartwatchextensions.NotificationChannelHelper
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.batterysync.Utils.BATTERY_STATS_NOTI_CHANNEL_ID
import com.boswelja.smartwatchextensions.batterysync.quicksettings.WatchBatteryTileService
import com.boswelja.smartwatchextensions.common.WatchWidgetProvider
import com.boswelja.smartwatchextensions.main.ui.MainActivity
import com.boswelja.smartwatchextensions.messages.Message
import com.boswelja.smartwatchextensions.messages.sendMessage
import com.boswelja.smartwatchextensions.settings.BoolSettingKeys
import com.boswelja.smartwatchextensions.settings.IntSettingKeys
import com.boswelja.smartwatchextensions.watchmanager.database.WatchDatabase
import com.boswelja.smartwatchextensions.watchmanager.database.WatchSettingsDatabase
import com.boswelja.watchconnection.common.Watch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Locale

private const val BATTERY_CHARGED_NOTI_ID = 408565
private const val BATTERY_LOW_NOTI_ID = 408566

class BatteryStatsReceiver : BaseBatteryStatsReceiver() {

    override suspend fun onBatteryStatsReceived(
        context: Context,
        sourceUid: String,
        batteryStats: BatteryStats
    ) {
        withContext(Dispatchers.IO) {
            val database = WatchDatabase.getInstance(context)
            database.getById(sourceUid).firstOrNull()?.let { watch ->
                val notificationManager = context.getSystemService<NotificationManager>()!!
                val settingsDb =
                    WatchSettingsDatabase.getInstance(context)
                if (batteryStats.charging) {
                    dismissLowNoti(
                        notificationManager,
                        settingsDb,
                        watch
                    )
                    handleWatchChargeNoti(
                        context,
                        notificationManager,
                        batteryStats,
                        settingsDb,
                        watch
                    )
                } else {
                    dismissChargeNoti(
                        notificationManager,
                        settingsDb,
                        watch
                    )
                    handleWatchLowNoti(
                        context,
                        notificationManager,
                        batteryStats,
                        settingsDb,
                        watch
                    )
                }
            }

            // Update battery stat widgets
            WatchWidgetProvider.updateWidgets(context)

            // Update QS Tile
            WatchBatteryTileService.requestTileUpdate(context)
        }
    }

    /**
     * Checks if we can send the watch charge notification, and either send or cancel it
     * appropriately.
     * @param batteryStats The [BatteryStats] to send a notification for.
     * @param database The [WatchSettingsDatabase] to access for settings.
     * @param watch The [Watch] to send a notification for.
     */
    suspend fun handleWatchChargeNoti(
        context: Context,
        notificationManager: NotificationManager,
        batteryStats: BatteryStats,
        database: WatchSettingsDatabase,
        watch: Watch
    ) {
        Timber.d("handleWatchChargeNoti called")
        val chargeThreshold = database.intSettings()
            .get(watch.uid, IntSettingKeys.BATTERY_CHARGE_THRESHOLD_KEY).firstOrNull()?.value ?: 90
        val shouldSendChargeNotis = database.boolSettings()
            .get(watch.uid, BoolSettingKeys.BATTERY_WATCH_CHARGE_NOTI_KEY).firstOrNull()?.value ?: false
        val hasSentNoti = database.boolSettings()
            .get(watch.uid, BoolSettingKeys.BATTERY_CHARGED_NOTI_SENT).firstOrNull()?.value ?: false
        Timber.d(
            "chargeThreshold = %s, shouldSendChargeNotis = %s, hasSentNoti = %s, percent = %s",
            chargeThreshold,
            shouldSendChargeNotis,
            hasSentNoti,
            batteryStats.percent
        )
        val shouldNotify = batteryStats.shouldPostChargeNotification(
            chargeThreshold,
            shouldSendChargeNotis,
            hasSentNoti
        )
        if (shouldNotify) {
            NotificationChannelHelper.createForBatteryStats(context, notificationManager)
            if (areNotificationsEnabled(context)) {
                Timber.i("Sending charged notification")
                NotificationCompat.Builder(
                    context,
                    Utils.BATTERY_STATS_NOTI_CHANNEL_ID
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
                Timber.w("Failed to send battery charged notification")
                context.sendMessage(
                    Message(
                        Message.Icon.ERROR,
                        context.getString(R.string.battery_charge_noti_issue_title),
                        context.getString(R.string.battery_charge_noti_issue_summary),
                        Message.Action.LAUNCH_NOTIFICATION_SETTINGS
                    )
                )
                database.boolSettings().updateByKey(BoolSettingKeys.BATTERY_WATCH_CHARGE_NOTI_KEY, false)
            }
            database.updateSetting(watch.uid, BoolSettingKeys.BATTERY_CHARGED_NOTI_SENT, true)
        }
    }

    /**
     * Checks if we can send the watch low notification, and either send or cancel it appropriately.
     * @param batteryStats The [BatteryStats] to send a notification for.
     * @param database The [WatchSettingsDatabase] to access for settings.
     * @param watch The [Watch] to send a notification for.
     */
    suspend fun handleWatchLowNoti(
        context: Context,
        notificationManager: NotificationManager,
        batteryStats: BatteryStats,
        database: WatchSettingsDatabase,
        watch: Watch
    ) {
        Timber.d("handleWatchLowNoti called")
        val lowThreshold = database.intSettings()
            .get(watch.uid, IntSettingKeys.BATTERY_LOW_THRESHOLD_KEY).firstOrNull()?.value ?: 15
        val shouldSendLowNoti = database.boolSettings()
            .get(watch.uid, BoolSettingKeys.BATTERY_WATCH_LOW_NOTI_KEY).firstOrNull()?.value ?: false
        val hasSentNoti = database.boolSettings()
            .get(watch.uid, BoolSettingKeys.BATTERY_LOW_NOTI_SENT).firstOrNull()?.value ?: false
        Timber.d(
            "lowThreshold = %s, shouldSendLowNoti = %s, hasSentNoti = %s, batteryPercent = %s",
            lowThreshold,
            shouldSendLowNoti,
            hasSentNoti,
            batteryStats.percent
        )

        // We can send a low noti if the user has enabled them, we haven't already sent it and
        // the watch is sufficiently discharged.
        val shouldNotify = batteryStats.shouldPostLowNotification(
            lowThreshold,
            shouldSendLowNoti,
            hasSentNoti
        )
        if (shouldNotify) {
            NotificationChannelHelper.createForBatteryStats(context, notificationManager)
            if (areNotificationsEnabled(context)) {
                Timber.i("Sending low notification")
                NotificationCompat.Builder(
                    context,
                    Utils.BATTERY_STATS_NOTI_CHANNEL_ID
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
                Timber.w("Failed to send battery low notification")
                context.sendMessage(
                    Message(
                        Message.Icon.ERROR,
                        context.getString(R.string.battery_low_noti_issue_title),
                        context.getString(R.string.battery_low_noti_issue_summary),
                        Message.Action.LAUNCH_NOTIFICATION_SETTINGS
                    )
                )
                database.boolSettings().updateByKey(BoolSettingKeys.BATTERY_WATCH_LOW_NOTI_KEY, false)
            }
            database.updateSetting(watch.uid, BoolSettingKeys.BATTERY_LOW_NOTI_SENT, true)
        }
    }

    suspend fun dismissChargeNoti(
        notificationManager: NotificationManager,
        database: WatchSettingsDatabase,
        watch: Watch
    ) {
        Timber.d("Dismissing charge notification for %s", watch.uid)
        notificationManager.cancel(BATTERY_CHARGED_NOTI_ID)
        database.updateSetting(watch.uid, BoolSettingKeys.BATTERY_CHARGED_NOTI_SENT, false)
    }

    suspend fun dismissLowNoti(
        notificationManager: NotificationManager,
        database: WatchSettingsDatabase,
        watch: Watch
    ) {
        Timber.d("Dismissing low notification for %s", watch.uid)
        notificationManager.cancel(BATTERY_LOW_NOTI_ID)
        database.updateSetting(watch.uid, BoolSettingKeys.BATTERY_LOW_NOTI_SENT, false)
    }

    private fun getNotiPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        return PendingIntent.getActivity(context, 123, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    /**
     * Checks whether notifications are enabled for the required channel.
     * @return true if notifications are enabled, false otherwise.
     */
    private fun areNotificationsEnabled(context: Context): Boolean {
        val notificationManager = context.getSystemService<NotificationManager>()!!
        notificationManager.getNotificationChannel(BATTERY_STATS_NOTI_CHANNEL_ID).let {
            return it != null && it.importance != NotificationManager.IMPORTANCE_NONE
        }
    }
}
