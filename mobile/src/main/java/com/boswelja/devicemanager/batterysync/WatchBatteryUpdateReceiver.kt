/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.batterysync

import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.NotificationChannelHelper
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.batterysync.database.WatchBatteryStats
import com.boswelja.devicemanager.batterysync.database.WatchBatteryStatsDatabase
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_CHARGED_NOTI_SENT
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_CHARGE_THRESHOLD_KEY
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_WATCH_CHARGE_NOTI_KEY
import com.boswelja.devicemanager.common.batterysync.References.BATTERY_STATUS_PATH
import com.boswelja.devicemanager.messages.Action
import com.boswelja.devicemanager.messages.Message
import com.boswelja.devicemanager.messages.database.MessageDatabase
import com.boswelja.devicemanager.watchmanager.BoolPreference
import com.boswelja.devicemanager.watchmanager.Watch
import com.boswelja.devicemanager.watchmanager.database.WatchDatabase
import com.boswelja.devicemanager.widgetdb.WidgetDatabase
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class WatchBatteryUpdateReceiver : WearableListenerService() {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private lateinit var notificationManager: NotificationManager
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var watchBatteryStats: WatchBatteryStats

    override fun onMessageReceived(messageEvent: MessageEvent?) {
        if (messageEvent?.path == BATTERY_STATUS_PATH) {
            Timber.i("Got BATTERY_STATUS_PATH")
            notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            watchBatteryStats = getBatteryStatsFromMessage(messageEvent)

            coroutineScope.launch {
                WatchDatabase.get(this@WatchBatteryUpdateReceiver).also {
                    val watch = it.watchDao().findByBatterySyncWorkerId(watchBatteryStats.watchId)
                    if (watch != null) {
                        handleNotification(it, watch)
                    } else {
                        Timber.w("Got watch battery stats from ${watchBatteryStats.watchId}, but watch is not registered")
                    }
                }

                WatchBatteryStatsDatabase.open(this@WatchBatteryUpdateReceiver).also {
                    Timber.i("Updating battery stats in database")
                    it.updateWatchBatteryStats(watchBatteryStats)
                    it.close()
                }
                WidgetDatabase.updateWatchWidgets(
                    this@WatchBatteryUpdateReceiver,
                    watchBatteryStats.watchId
                )
            }
        }
    }

    /**
     * Gets battery information from a [MessageEvent].
     * @param messageEvent The [MessageEvent] containing battery information.
     * @return A new [WatchBatteryStats] with data.
     */
    private fun getBatteryStatsFromMessage(messageEvent: MessageEvent): WatchBatteryStats {
        val watchId = messageEvent.sourceNodeId
        val message = String(messageEvent.data, Charsets.UTF_8)
        val messageSplit = message.split("|")
        val batteryPercent = messageSplit[0].toInt()
        val isWatchCharging = messageSplit[1] == true.toString()
        Timber.i("Got WatchBatteryStats from MessageEvent")
        return WatchBatteryStats(watchId, batteryPercent, isWatchCharging)
    }

    private fun handleNotification(database: WatchDatabase, watch: Watch) {
        if (canSendChargedNotification(database, watch.id)) {
            notifyWatchCharged(watch)
            database.boolPreferenceDao().update(
                    BoolPreference(watch.id, BATTERY_CHARGED_NOTI_SENT, true))
        } else {
            notificationManager.cancel(BATTERY_CHARGED_NOTI_ID)
            database.boolPreferenceDao().update(
                    BoolPreference(watch.id, BATTERY_CHARGED_NOTI_SENT, false))
        }
    }

    /**
     * Checks whether we can notify the user their watch is charged.
     * @return true if we can send a charged notification, false otherwise.
     */
    private fun canSendChargedNotification(database: WatchDatabase, watchId: String): Boolean {
        val sendChargeNotis =
                database.boolPreferenceDao().getWhere(watchId, BATTERY_WATCH_CHARGE_NOTI_KEY)?.value == true
        val chargedThreshold =
                database.intPreferenceDao().getWhere(watchId, BATTERY_CHARGE_THRESHOLD_KEY)?.value ?: 90
        val chargedNotiSent =
                database.boolPreferenceDao().getWhere(watchId, BATTERY_CHARGED_NOTI_SENT)?.value == true
        return watchBatteryStats.isWatchCharging && sendChargeNotis &&
                (watchBatteryStats.batteryPercent >= chargedThreshold) &&
                !chargedNotiSent
    }

    /**
     * Notify the user their watch is charged.
     * @param watch The [Watch] to send a notification for.
     */
    private fun notifyWatchCharged(watch: Watch) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            NotificationChannelHelper.createForBatteryCharged(this, notificationManager)

        if (Compat.areNotificationsEnabled(this, BATTERY_CHARGED_NOTI_CHANNEL_ID)) {
            Timber.i("Sending charged notification")
            NotificationCompat.Builder(this, BATTERY_CHARGED_NOTI_CHANNEL_ID)
                .setSmallIcon(R.drawable.battery_full)
                .setContentTitle(getString(R.string.device_charged_noti_title, watch.name))
                .setContentText(
                    getString(R.string.device_charged_noti_desc)
                        .format(watch.name, watch.intPrefs[BATTERY_CHARGE_THRESHOLD_KEY] ?: 90)
                )
                .setLocalOnly(true)
                .also {
                    notificationManager.notify(BATTERY_CHARGED_NOTI_ID, it.build())
                }
        } else {
            Timber.w("Failed to send charged notification")
            coroutineScope.launch(Dispatchers.IO) {
                MessageDatabase.open(this@WatchBatteryUpdateReceiver).apply {
                    val message = Message(
                        iconRes = R.drawable.pref_ic_warning,
                        label = getString(R.string.message_watch_charge_noti_warning_label),
                        shortLabel = getString(R.string.message_watch_charge_noti_warning_label_short),
                        desc = getString(R.string.message_watch_charge_noti_warning_desc),
                        buttonLabel = getString(R.string.message_watch_charge_noti_warning_button_label),
                        action = Action.LAUNCH_NOTIFICATION_SETTINGS
                    )
                    sendMessage(sharedPreferences, message)
                }.also {
                    it.close()
                }
            }
        }
    }

    companion object {
        const val BATTERY_CHARGED_NOTI_CHANNEL_ID = "companion_device_charged"
        const val BATTERY_CHARGED_NOTI_ID = 408565
    }
}
