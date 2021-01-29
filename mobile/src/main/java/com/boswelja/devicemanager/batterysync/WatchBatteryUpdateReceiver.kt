/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.batterysync

import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.boswelja.devicemanager.NotificationChannelHelper
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.batterysync.database.WatchBatteryStats
import com.boswelja.devicemanager.batterysync.database.WatchBatteryStatsDatabase
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.batterysync.References.BATTERY_STATUS_PATH
import com.boswelja.devicemanager.common.preference.PreferenceKey.BATTERY_CHARGED_NOTI_SENT
import com.boswelja.devicemanager.common.preference.PreferenceKey.BATTERY_CHARGE_THRESHOLD_KEY
import com.boswelja.devicemanager.common.preference.PreferenceKey.BATTERY_WATCH_CHARGE_NOTI_KEY
import com.boswelja.devicemanager.watchmanager.database.WatchDatabase
import com.boswelja.devicemanager.watchmanager.item.BoolPreference
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.boswelja.devicemanager.widget.database.WidgetDatabase
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
            Timber.i("Got BATTERY_STATUS_PATH")
            watchBatteryStats = WatchBatteryStats.fromMessage(messageEvent)

            coroutineScope.launch {
                val database = WatchDatabase.getInstance(this@WatchBatteryUpdateReceiver)
                database.watchDao().get(watchBatteryStats.watchId)?.let {
                    handleNoti(database, it)
                }
                updateStatsInDatabase()
                WidgetDatabase.updateWatchWidgets(
                    this@WatchBatteryUpdateReceiver, watchBatteryStats.watchId
                )
            }
        }
    }

    private fun updateStatsInDatabase() {
        WatchBatteryStatsDatabase.get(this).batteryStatsDao().updateStats(watchBatteryStats)
    }

    private fun handleNoti(database: WatchDatabase, watch: Watch) {
        val chargedThreshold =
            database.intPrefDao().get(watch.id, BATTERY_CHARGE_THRESHOLD_KEY)?.value ?: 90
        if (canSendChargedNoti(database, watch.id, chargedThreshold)) {
            notifyWatchCharged(watch, chargedThreshold)
            database.boolPrefDao().update(BoolPreference(watch.id, BATTERY_CHARGED_NOTI_SENT, true))
        } else {
            notificationManager.cancel(BATTERY_CHARGED_NOTI_ID)
            database
                .boolPrefDao()
                .update(BoolPreference(watch.id, BATTERY_CHARGED_NOTI_SENT, false))
        }
    }

    /**
     * Checks whether we can notify the user their watch is charged.
     * @return true if we can send a charged notification, false otherwise.
     */
    private fun canSendChargedNoti(
        database: WatchDatabase,
        watchId: String,
        chargedThreshold: Int
    ): Boolean {
        val sendChargeNotis =
            database.boolPrefDao().get(watchId, BATTERY_WATCH_CHARGE_NOTI_KEY)?.value == true
        val chargedNotiSent =
            database.boolPrefDao().get(watchId, BATTERY_CHARGED_NOTI_SENT)?.value == true
        return watchBatteryStats.isCharging &&
            sendChargeNotis &&
            (watchBatteryStats.percent >= chargedThreshold) &&
            !chargedNotiSent
    }

    /**
     * Notify the user their watch is charged.
     * @param watch The [Watch] to send a notification for.
     */
    private fun notifyWatchCharged(watch: Watch, chargeThreshold: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            NotificationChannelHelper.createForBatteryCharged(this, notificationManager)

        if (Compat.areNotificationsEnabled(this, BATTERY_CHARGED_NOTI_CHANNEL_ID)) {
            Timber.i("Sending charged notification")
            NotificationCompat.Builder(this, BATTERY_CHARGED_NOTI_CHANNEL_ID)
                .setSmallIcon(R.drawable.battery_full)
                .setContentTitle(getString(R.string.device_charged_noti_title, watch.name))
                .setContentText(
                    getString(R.string.device_charged_noti_desc)
                        .format(Locale.getDefault(), watch.name, chargeThreshold)
                )
                .setLocalOnly(true)
                .also { notificationManager.notify(BATTERY_CHARGED_NOTI_ID, it.build()) }
        } else {
            Timber.w("Failed to send charged notification")
        }
    }

    companion object {
        const val BATTERY_CHARGED_NOTI_CHANNEL_ID = "companion_device_charged"
        const val BATTERY_CHARGED_NOTI_ID = 408565
    }
}
