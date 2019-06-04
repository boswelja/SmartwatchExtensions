/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.common.prefsynclayer

import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.common.prefsynclayer.PreferenceSyncKeys.BATTERY_PHONE_FULL_CHARGE_NOTI_KEY
import com.boswelja.devicemanager.common.prefsynclayer.PreferenceSyncKeys.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.devicemanager.common.prefsynclayer.PreferenceSyncKeys.BATTERY_WATCH_FULL_CHARGE_NOTI_KEY
import com.boswelja.devicemanager.common.prefsynclayer.PreferenceSyncKeys.DND_SYNC_PHONE_TO_WATCH_KEY
import com.boswelja.devicemanager.common.prefsynclayer.PreferenceSyncKeys.DND_SYNC_WATCH_TO_PHONE_KEY
import com.boswelja.devicemanager.common.prefsynclayer.PreferenceSyncKeys.DND_SYNC_WITH_THEATER_KEY
import com.boswelja.devicemanager.common.prefsynclayer.PreferenceSyncKeys.LOCK_PHONE_ENABLED_KEY
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

abstract class BasePreferenceChangeReceiver : WearableListenerService() {

    lateinit var prefs: SharedPreferences

    override fun onDataChanged(dataEvents: DataEventBuffer?) {
        super.onDataChanged(dataEvents)
        dataEvents?.forEach { event ->
            val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap

            val batterySyncEnabled = dataMap.getBoolean(BATTERY_SYNC_ENABLED_KEY)
            val batteryPhoneChargedNoti = dataMap.getBoolean(BATTERY_PHONE_FULL_CHARGE_NOTI_KEY)
            val batteryWatchChargedNoti = dataMap.getBoolean(BATTERY_WATCH_FULL_CHARGE_NOTI_KEY)

            val interruptFilterSyncToPhone = dataMap.getBoolean(DND_SYNC_WATCH_TO_PHONE_KEY)
            val interruptFilterSyncToWatch = dataMap.getBoolean(DND_SYNC_PHONE_TO_WATCH_KEY)
            val interruptFilterSyncWithTheater = dataMap.getBoolean(DND_SYNC_WITH_THEATER_KEY)

            val lockPhoneEnabled = dataMap.getBoolean(LOCK_PHONE_ENABLED_KEY)

            prefs = PreferenceManager.getDefaultSharedPreferences(this)
            prefs.edit()
                    .putBoolean(PreferenceKey.BATTERY_SYNC_ENABLED_KEY, batterySyncEnabled)
                    .putBoolean(PreferenceKey.BATTERY_PHONE_CHARGE_NOTI_KEY, batteryPhoneChargedNoti)
                    .putBoolean(PreferenceKey.BATTERY_WATCH_CHARGE_NOTI_KEY, batteryWatchChargedNoti)
                    .putBoolean(PreferenceKey.INTERRUPT_FILTER_SYNC_TO_PHONE_KEY, interruptFilterSyncToPhone)
                    .putBoolean(PreferenceKey.INTERRUPT_FILTER_SYNC_TO_WATCH_KEY, interruptFilterSyncToWatch)
                    .putBoolean(PreferenceKey.INTERRUPT_FILTER_ON_WITH_THEATER_KEY, interruptFilterSyncWithTheater)
                    .putBoolean(PreferenceKey.PHONE_LOCKING_ENABLED_KEY, lockPhoneEnabled)
                    .apply()

            handleStartServices(
                    interruptFilterSyncToPhone,
                    interruptFilterSyncToWatch,
                    interruptFilterSyncWithTheater,
                    batterySyncEnabled)
        }
    }

    abstract fun handleStartServices(
        interruptFilterSyncToPhone: Boolean,
        interruptFilterSyncToWatch: Boolean,
        interruptFilterSyncWithTheater: Boolean,
        batterySyncEnabled: Boolean
    )
}
