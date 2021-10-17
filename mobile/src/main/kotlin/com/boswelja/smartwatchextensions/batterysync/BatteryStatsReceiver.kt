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
import com.boswelja.smartwatchextensions.devicemanagement.WatchRepository
import com.boswelja.smartwatchextensions.main.ui.MainActivity
import com.boswelja.smartwatchextensions.messages.Message
import com.boswelja.smartwatchextensions.messages.MessagesRepository
import com.boswelja.smartwatchextensions.messages.sendMessage
import com.boswelja.smartwatchextensions.settings.BoolSettingKeys.BATTERY_CHARGED_NOTI_SENT
import com.boswelja.smartwatchextensions.settings.BoolSettingKeys.BATTERY_LOW_NOTI_SENT
import com.boswelja.smartwatchextensions.settings.BoolSettingKeys.BATTERY_WATCH_CHARGE_NOTI_KEY
import com.boswelja.smartwatchextensions.settings.BoolSettingKeys.BATTERY_WATCH_LOW_NOTI_KEY
import com.boswelja.smartwatchextensions.settings.IntSettingKeys.BATTERY_CHARGE_THRESHOLD_KEY
import com.boswelja.smartwatchextensions.settings.IntSettingKeys.BATTERY_LOW_THRESHOLD_KEY
import com.boswelja.smartwatchextensions.settings.WatchSettingsRepository
import com.boswelja.watchconnection.common.Watch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.kodein.di.DIAware
import org.kodein.di.LateInitDI
import org.kodein.di.instance
import timber.log.Timber
import java.util.Locale

private const val BATTERY_CHARGED_NOTI_ID = 408565
private const val BATTERY_LOW_NOTI_ID = 408566

class BatteryStatsReceiver : BaseBatteryStatsReceiver(), DIAware {

    override val di = LateInitDI()

    private val messagesRepository: MessagesRepository by instance()
    private val watchRepository: WatchRepository by instance()
    private val settingsRepository: WatchSettingsRepository by instance()

    override suspend fun onBatteryStatsReceived(
        context: Context,
        sourceUid: String,
        batteryStats: BatteryStats
    ) {
        di.baseDI = (context.applicationContext as DIAware).di
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
     * @param watch The [Watch] to send a notification for.
     */
    suspend fun handleWatchChargeNoti(
        context: Context,
        notificationManager: NotificationManager,
        batteryStats: BatteryStats,
        watch: Watch
    ) {
        val chargeThreshold = settingsRepository
            .getInt(watch.uid, BATTERY_CHARGE_THRESHOLD_KEY, 90)
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
            NotificationChannelHelper.createForBatteryStats(context, notificationManager)
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
                context.sendMessage(
                    Message(
                        Message.Icon.ERROR,
                        context.getString(R.string.battery_charge_noti_issue_title),
                        context.getString(R.string.battery_charge_noti_issue_summary),
                        Message.Action.NOTIFICATION_SETTINGS
                    ),
                    repository = messagesRepository
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
    suspend fun handleWatchLowNoti(
        context: Context,
        notificationManager: NotificationManager,
        batteryStats: BatteryStats,
        watch: Watch
    ) {
        val lowThreshold = settingsRepository
            .getInt(watch.uid, BATTERY_LOW_THRESHOLD_KEY, 90)
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
            NotificationChannelHelper.createForBatteryStats(context, notificationManager)
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
                Timber.w("Failed to send battery low notification")
                context.sendMessage(
                    Message(
                        Message.Icon.ERROR,
                        context.getString(R.string.battery_low_noti_issue_title),
                        context.getString(R.string.battery_low_noti_issue_summary),
                        Message.Action.NOTIFICATION_SETTINGS
                    ),
                    repository = messagesRepository
                )
            }
            settingsRepository.putBoolean(watch.uid, BATTERY_LOW_NOTI_SENT, true)
        }
    }

    suspend fun dismissChargeNoti(
        notificationManager: NotificationManager,
        watch: Watch
    ) {
        Timber.d("Dismissing charge notification for %s", watch.uid)
        notificationManager.cancel(BATTERY_CHARGED_NOTI_ID)
        settingsRepository.putBoolean(watch.uid, BATTERY_CHARGED_NOTI_SENT, false)
    }

    suspend fun dismissLowNoti(
        notificationManager: NotificationManager,
        watch: Watch
    ) {
        Timber.d("Dismissing low notification for %s", watch.uid)
        notificationManager.cancel(BATTERY_LOW_NOTI_ID)
        settingsRepository.putBoolean(watch.uid, BATTERY_LOW_NOTI_SENT, false)
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
