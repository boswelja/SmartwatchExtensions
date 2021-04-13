package com.boswelja.devicemanager.batterysync

import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.boswelja.devicemanager.NotificationChannelHelper
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.batterysync.database.WatchBatteryStats
import com.boswelja.devicemanager.batterysync.database.WatchBatteryStatsDatabase
import com.boswelja.devicemanager.batterysync.widget.WatchBatteryWidget
import com.boswelja.devicemanager.common.batterysync.References.BATTERY_STATUS_PATH
import com.boswelja.devicemanager.common.preference.PreferenceKey.BATTERY_CHARGED_NOTI_SENT
import com.boswelja.devicemanager.common.preference.PreferenceKey.BATTERY_CHARGE_THRESHOLD_KEY
import com.boswelja.devicemanager.common.preference.PreferenceKey.BATTERY_LOW_NOTI_SENT
import com.boswelja.devicemanager.common.preference.PreferenceKey.BATTERY_LOW_THRESHOLD_KEY
import com.boswelja.devicemanager.common.preference.PreferenceKey.BATTERY_WATCH_CHARGE_NOTI_KEY
import com.boswelja.devicemanager.common.preference.PreferenceKey.BATTERY_WATCH_LOW_NOTI_KEY
import com.boswelja.devicemanager.common.ui.BaseWidgetProvider
import com.boswelja.devicemanager.watchmanager.database.WatchDatabase
import com.boswelja.devicemanager.watchmanager.database.WatchSettingsDatabase
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class WatchBatteryUpdateReceiver : WearableListenerService() {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val notificationManager: NotificationManager by lazy { getSystemService()!! }

    private lateinit var watchBatteryStats: WatchBatteryStats

    override fun onMessageReceived(messageEvent: MessageEvent?) {
        if (messageEvent?.path == BATTERY_STATUS_PATH) {
            Timber.i("Got ${messageEvent.path}")
            watchBatteryStats = WatchBatteryStats.fromMessage(messageEvent)

            // TODO We shouldn't need to launch a coroutine scope here
            coroutineScope.launch {
                val database = WatchDatabase.getInstance(this@WatchBatteryUpdateReceiver)
                database.getById(watchBatteryStats.watchId)?.let {
                    val settingsDb = WatchSettingsDatabase.getInstance(this@WatchBatteryUpdateReceiver)
                    if (watchBatteryStats.isCharging) {
                        handleWatchChargeNoti(
                            watchBatteryStats,
                            settingsDb,
                            it
                        )
                    } else {
                        handleWatchLowNoti(
                            watchBatteryStats,
                            settingsDb,
                            it
                        )
                    }
                }
                updateStatsInDatabase()
                updateWidgetsForWatch()
            }
        }
    }

    private fun updateStatsInDatabase() {
        WatchBatteryStatsDatabase.getInstance(this).batteryStatsDao().updateStats(watchBatteryStats)
    }

    /**
     * Update all instances of [WatchBatteryWidget] associated with a given watch ID.
     */
    private fun updateWidgetsForWatch() {
        Timber.d("updateWidgetsForWatch called")
        // Fallback to updating all widgets if database isn't open
        BaseWidgetProvider.updateWidgets(this)
    }

    /**
     * Checks if we can send the watch charge notification, and either send or cancel it
     * appropriately.
     * @param batteryStats The [WatchBatteryStats] to send a notification for.
     * @param database The [WatchSettingsDatabase] to access for settings.
     * @param watch The [Watch] to send a notification for.
     */
    private suspend fun handleWatchChargeNoti(
        batteryStats: WatchBatteryStats,
        database: WatchSettingsDatabase,
        watch: Watch
    ) {
        val chargeThreshold = database
            .getPreference<Int>(watch.id, BATTERY_CHARGE_THRESHOLD_KEY)?.value ?: 90
        val shouldSendChargeNotis = database
            .getPreference<Boolean>(watch.id, BATTERY_WATCH_CHARGE_NOTI_KEY)?.value ?: false
        val hasSentNoti = database
            .getPreference<Boolean>(watch.id, BATTERY_CHARGED_NOTI_SENT)?.value ?: false
        // We can send a charge noti if the user has enabled them, we haven't already sent it and
        // the watch is sufficiently charged.
        val canSendChargeNoti =
            shouldSendChargeNotis && hasSentNoti && batteryStats.percent >= chargeThreshold
        if (canSendChargeNoti) {
            NotificationChannelHelper.createForBatteryStats(this, notificationManager)
            if (areNotificationsEnabled()) {
                Timber.i("Sending charged notification")
                NotificationCompat.Builder(this, BATTERY_STATS_NOTI_CHANNEL_ID)
                    .setSmallIcon(R.drawable.battery_full)
                    .setContentTitle(getString(R.string.device_charged_noti_title, watch.name))
                    .setContentText(
                        getString(R.string.device_charged_noti_desc)
                            .format(Locale.getDefault(), watch.name, chargeThreshold)
                    )
                    .setLocalOnly(true)
                    .also { notificationManager.notify(BATTERY_CHARGED_NOTI_ID, it.build()) }
            } else {
                // TODO Send a message informing the user of the issue
                Timber.w("Failed to send charged notification")
            }
            database.updatePrefInDatabase(watch.id, BATTERY_CHARGED_NOTI_SENT, true)
        } else {
            notificationManager.cancel(BATTERY_CHARGED_NOTI_ID)
            database.updatePrefInDatabase(watch.id, BATTERY_CHARGED_NOTI_SENT, false)
        }
    }

    /**
     * Checks if we can send the watch low notification, and either send or cancel it appropriately.
     * @param batteryStats The [WatchBatteryStats] to send a notification for.
     * @param database The [WatchSettingsDatabase] to access for settings.
     * @param watch The [Watch] to send a notification for.
     */
    private suspend fun handleWatchLowNoti(
        batteryStats: WatchBatteryStats,
        database: WatchSettingsDatabase,
        watch: Watch
    ) {
        val chargeThreshold = database
            .getPreference<Int>(watch.id, BATTERY_LOW_THRESHOLD_KEY)?.value ?: 15
        val shouldSendChargeNotis = database
            .getPreference<Boolean>(watch.id, BATTERY_WATCH_LOW_NOTI_KEY)?.value ?: false
        val hasSentNoti = database
            .getPreference<Boolean>(watch.id, BATTERY_LOW_NOTI_SENT)?.value ?: false
        // We can send a low noti if the user has enabled them, we haven't already sent it and
        // the watch is sufficiently discharged.
        val canSendLowNoti =
            shouldSendChargeNotis && hasSentNoti && batteryStats.percent <= chargeThreshold
        if (canSendLowNoti) {
            NotificationChannelHelper.createForBatteryStats(this, notificationManager)
            if (areNotificationsEnabled()) {
                Timber.i("Sending low notification")
                NotificationCompat.Builder(this, BATTERY_STATS_NOTI_CHANNEL_ID)
                    .setSmallIcon(R.drawable.battery_alert)
                    .setContentTitle(getString(R.string.device_charged_noti_title, watch.name))
                    .setContentText(
                        getString(R.string.device_charged_noti_desc)
                            .format(Locale.getDefault(), watch.name, chargeThreshold)
                    )
                    .setLocalOnly(true)
                    .also { notificationManager.notify(BATTERY_LOW_NOTI_ID, it.build()) }
            } else {
                // TODO Send a message informing the user of the issue
                Timber.w("Failed to send charged notification")
            }
            database.updatePrefInDatabase(watch.id, BATTERY_LOW_NOTI_SENT, true)
        } else {
            notificationManager.cancel(BATTERY_LOW_NOTI_ID)
            database.updatePrefInDatabase(watch.id, BATTERY_LOW_NOTI_SENT, false)
        }
    }

    /**
     * Checks whether notifications are enabled for the required channel.
     * @return true if notifications are enabled, false otherwise.
     */
    private fun areNotificationsEnabled(): Boolean {
        val notificationManager = getSystemService(NotificationManager::class.java)
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
