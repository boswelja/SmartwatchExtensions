/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.common.prefsynclayer

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.common.PreferenceKey
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

abstract class BasePreferenceChangeReceiver : WearableListenerService() {

    lateinit var prefs: SharedPreferences

    override fun onDataChanged(dataEvents: DataEventBuffer?) {
        super.onDataChanged(dataEvents)
        dataEvents?.forEach { event ->
            val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
            if (!dataMap.isEmpty) {
                prefs = PreferenceManager.getDefaultSharedPreferences(this)
                prefs.edit {
                    for (key in dataMap.keySet()) {
                        when (key) {
                            PreferenceKey.PHONE_LOCKING_ENABLED_KEY,
                            PreferenceKey.BATTERY_SYNC_ENABLED_KEY,
                            PreferenceKey.BATTERY_PHONE_CHARGE_NOTI_KEY,
                            PreferenceKey.BATTERY_WATCH_CHARGE_NOTI_KEY,
                            PreferenceKey.INTERRUPT_FILTER_SYNC_TO_WATCH_KEY,
                            PreferenceKey.INTERRUPT_FILTER_SYNC_TO_PHONE_KEY,
                            PreferenceKey.INTERRUPT_FILTER_ON_WITH_THEATER_KEY -> {
                                val newValue = dataMap.getBoolean(key)
                                putBoolean(key, newValue)
                                onPreferenceChanged(key, newValue)
                            }
                            PreferenceKey.BATTERY_CHARGE_THRESHOLD_KEY -> {
                                val newValue = dataMap.getInt(key)
                                putInt(key, newValue)
                                onPreferenceChanged(key, newValue)
                            }
                        }
                    }
                }
            }
        }
    }

    abstract fun onPreferenceChanged(key: String, newValue: Any)
}
