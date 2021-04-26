package com.boswelja.smartwatchextensions.watchmanager.connection

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.boswelja.smartwatchextensions.NotificationChannelHelper
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.batterysync.Utils
import com.boswelja.smartwatchextensions.batterysync.database.WatchBatteryStats.Companion.toWatchBatteryStats
import com.boswelja.smartwatchextensions.batterysync.database.WatchBatteryStatsDatabase
import com.boswelja.smartwatchextensions.common.Compat
import com.boswelja.smartwatchextensions.common.batterysync.BatteryStats
import com.boswelja.smartwatchextensions.common.batterysync.References.BATTERY_STATUS_PATH
import com.boswelja.smartwatchextensions.common.batterysync.References.REQUEST_BATTERY_UPDATE_PATH
import com.boswelja.smartwatchextensions.common.connection.Capability
import com.boswelja.smartwatchextensions.common.connection.Messages.CHECK_WATCH_REGISTERED_PATH
import com.boswelja.smartwatchextensions.common.connection.Messages.LAUNCH_APP
import com.boswelja.smartwatchextensions.common.connection.Messages.WATCH_REGISTERED_PATH
import com.boswelja.smartwatchextensions.common.dndsync.References.REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH
import com.boswelja.smartwatchextensions.common.preference.PreferenceKey
import com.boswelja.smartwatchextensions.common.toByteArray
import com.boswelja.smartwatchextensions.common.ui.BaseWidgetProvider
import com.boswelja.smartwatchextensions.main.MainActivity
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import com.boswelja.smartwatchextensions.watchmanager.database.WatchDatabase
import com.boswelja.smartwatchextensions.watchmanager.database.WatchSettingsDatabase
import com.boswelja.watchconnection.core.MessageReceiver
import com.boswelja.watchconnection.core.Watch
import com.boswelja.watchconnection.core.WatchPlatformManager
import com.boswelja.watchconnection.wearos.WearOSPlatform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Locale
import java.util.UUID

class WatchMessageReceiver : MessageReceiver() {

    override suspend fun onMessageReceived(
        context: Context,
        sourceWatchId: UUID,
        message: String,
        data: ByteArray?
    ) {
        when (message) {
            LAUNCH_APP -> launchApp(context)
            REQUEST_BATTERY_UPDATE_PATH -> Utils.updateBatteryStats(context)
            REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH ->
                sendInterruptFilterAccess(context, sourceWatchId)
            CHECK_WATCH_REGISTERED_PATH -> sendIsWatchRegistered(context, sourceWatchId)
            BATTERY_STATUS_PATH -> data?.let {
                handleBatteryStats(context, sourceWatchId, BatteryStats.fromByteArray(data))
            }
        }
    }

    /**
     * Launches Smartwatch Extensions to an activity containing a specified preference key.
     * @param context [Context].
     */
    private fun launchApp(context: Context) {
        Timber.i("launchApp() called")
        Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }.also {
            context.startActivity(it)
        }
    }

    /**
     * Tells the source node whether we are allowed to set the state of Do not Disturb.
     * @param context [Context].
     * @param watchId The target watch ID to send the response to.
     */
    private suspend fun sendInterruptFilterAccess(context: Context, watchId: UUID) {
        Timber.i("sendInterruptFilterAccess() called")
        withContext(Dispatchers.IO) {
            val database = WatchDatabase.getInstance(context)
            val watch = database.watchDao().get(watchId)
            watch?.let {
                val hasDnDAccess = Compat.canSetDnD(context)
                WatchPlatformManager(
                    WearOSPlatform(
                        context,
                        WatchManager.CAPABILITY_WATCH_APP,
                        Capability.values().map { it.name }
                    )
                ).sendMessage(
                    watch,
                    REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH,
                    hasDnDAccess.toByteArray()
                )
            }
        }
    }

    /**
     * Tells the source node whether it is registered with Smartwatch Extensions.
     * @param context [Context].
     * @param watchId The target watch ID to send the response to.
     */
    private suspend fun sendIsWatchRegistered(context: Context, watchId: UUID) {
        Timber.i("sendIsWatchRegistered() called")
        withContext(Dispatchers.IO) {
            val database = WatchDatabase.getInstance(context)
            val watch = database.watchDao().get(watchId)
            // If watch is found in the database, let it know it's registered
            watch?.let {
                WatchPlatformManager(
                    WearOSPlatform(
                        context,
                        WatchManager.CAPABILITY_WATCH_APP,
                        Capability.values().map { it.name }
                    )
                ).sendMessage(watch, WATCH_REGISTERED_PATH)
            }
        }
    }

    private suspend fun handleBatteryStats(
        context: Context,
        watchId: UUID,
        batteryStats: BatteryStats
    ) {
        withContext(Dispatchers.IO) {
            val database = WatchDatabase.getInstance(context)
            database.watchDao().get(watchId)?.let { watch ->
                val settingsDb =
                    WatchSettingsDatabase.getInstance(context)
                if (batteryStats.isCharging) {
                    handleWatchChargeNoti(
                        context,
                        batteryStats,
                        settingsDb,
                        watch
                    )
                } else {
                    handleWatchLowNoti(
                        context,
                        batteryStats,
                        settingsDb,
                        watch
                    )
                }
            }
            // Update stats in database
            WatchBatteryStatsDatabase.getInstance(context)
                .batteryStatsDao().updateStats(batteryStats.toWatchBatteryStats(watchId))

            // Update battery stat widgets
            BaseWidgetProvider.updateWidgets(context)
        }
    }

    /**
     * Checks if we can send the watch charge notification, and either send or cancel it
     * appropriately.
     * @param batteryStats The [BatteryStats] to send a notification for.
     * @param database The [WatchSettingsDatabase] to access for settings.
     * @param watch The [Watch] to send a notification for.
     */
    private suspend fun handleWatchChargeNoti(
        context: Context,
        batteryStats: BatteryStats,
        database: WatchSettingsDatabase,
        watch: Watch
    ) {
        val notificationManager = context.getSystemService<NotificationManager>()!!
        val chargeThreshold = database
            .getPreference<Int>(watch.id, PreferenceKey.BATTERY_CHARGE_THRESHOLD_KEY)?.value ?: 90
        val shouldSendChargeNotis = database
            .getPreference<Boolean>(watch.id, PreferenceKey.BATTERY_WATCH_CHARGE_NOTI_KEY)?.value ?: false
        val hasSentNoti = database
            .getPreference<Boolean>(watch.id, PreferenceKey.BATTERY_CHARGED_NOTI_SENT)?.value ?: false
        // We can send a charge noti if the user has enabled them, we haven't already sent it and
        // the watch is sufficiently charged.
        val canSendChargeNoti =
            shouldSendChargeNotis && hasSentNoti && batteryStats.percent >= chargeThreshold
        if (canSendChargeNoti) {
            NotificationChannelHelper.createForBatteryStats(context, notificationManager)
            if (areNotificationsEnabled(context)) {
                Timber.i("Sending charged notification")
                NotificationCompat.Builder(context, BATTERY_STATS_NOTI_CHANNEL_ID)
                    .setSmallIcon(R.drawable.battery_full)
                    .setContentTitle(
                        context.getString(R.string.device_battery_charged_noti_title, watch.name)
                    )
                    .setContentText(
                        context.getString(R.string.device_battery_charged_noti_desc)
                            .format(Locale.getDefault(), watch.name, chargeThreshold)
                    )
                    .setLocalOnly(true)
                    .also { notificationManager.notify(BATTERY_CHARGED_NOTI_ID, it.build()) }
            } else {
                // TODO Send a message informing the user of the issue
                Timber.w("Failed to send charged notification")
            }
            database.updatePrefInDatabase(watch.id, PreferenceKey.BATTERY_CHARGED_NOTI_SENT, true)
        } else {
            notificationManager.cancel(BATTERY_CHARGED_NOTI_ID)
            database.updatePrefInDatabase(watch.id, PreferenceKey.BATTERY_CHARGED_NOTI_SENT, false)
        }
    }

    /**
     * Checks if we can send the watch low notification, and either send or cancel it appropriately.
     * @param batteryStats The [BatteryStats] to send a notification for.
     * @param database The [WatchSettingsDatabase] to access for settings.
     * @param watch The [Watch] to send a notification for.
     */
    private suspend fun handleWatchLowNoti(
        context: Context,
        batteryStats: BatteryStats,
        database: WatchSettingsDatabase,
        watch: Watch
    ) {
        val notificationManager = context.getSystemService<NotificationManager>()!!
        val chargeThreshold = database
            .getPreference<Int>(watch.id, PreferenceKey.BATTERY_LOW_THRESHOLD_KEY)?.value ?: 15
        val shouldSendChargeNotis = database
            .getPreference<Boolean>(watch.id, PreferenceKey.BATTERY_WATCH_LOW_NOTI_KEY)?.value ?: false
        val hasSentNoti = database
            .getPreference<Boolean>(watch.id, PreferenceKey.BATTERY_LOW_NOTI_SENT)?.value ?: false
        // We can send a low noti if the user has enabled them, we haven't already sent it and
        // the watch is sufficiently discharged.
        val canSendLowNoti =
            shouldSendChargeNotis && hasSentNoti && batteryStats.percent <= chargeThreshold
        if (canSendLowNoti) {
            NotificationChannelHelper.createForBatteryStats(context, notificationManager)
            if (areNotificationsEnabled(context)) {
                Timber.i("Sending low notification")
                NotificationCompat.Builder(context, BATTERY_STATS_NOTI_CHANNEL_ID)
                    .setSmallIcon(R.drawable.battery_alert)
                    .setContentTitle(context.getString(R.string.device_battery_low_noti_title, watch.name))
                    .setContentText(
                        context.getString(R.string.device_battery_low_noti_desc)
                            .format(Locale.getDefault(), watch.name, chargeThreshold)
                    )
                    .setLocalOnly(true)
                    .also { notificationManager.notify(BATTERY_LOW_NOTI_ID, it.build()) }
            } else {
                // TODO Send a message informing the user of the issue
                Timber.w("Failed to send charged notification")
            }
            database.updatePrefInDatabase(watch.id, PreferenceKey.BATTERY_LOW_NOTI_SENT, true)
        } else {
            notificationManager.cancel(BATTERY_LOW_NOTI_ID)
            database.updatePrefInDatabase(watch.id, PreferenceKey.BATTERY_LOW_NOTI_SENT, false)
        }
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

    companion object {
        const val BATTERY_STATS_NOTI_CHANNEL_ID = "companion_device_charged"
        const val BATTERY_CHARGED_NOTI_ID = 408565
        const val BATTERY_LOW_NOTI_ID = 408566
    }
}
