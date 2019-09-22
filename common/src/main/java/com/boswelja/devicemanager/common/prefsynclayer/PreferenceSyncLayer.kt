/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.common.prefsynclayer

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.common.PreferenceKey
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable

class PreferenceSyncLayer(context: Context, nodeId: String) {

    private val localPrefs = PreferenceManager.getDefaultSharedPreferences(context)
    private val dataClient = Wearable.getDataClient(context)
    private val preferenceChangePath = "/preference-change_$nodeId"

    init {
        getCurrentData()
                .addOnSuccessListener {
                    val dataMap = DataMapItem.fromDataItem(it).dataMap
                    if (!dataMap.isEmpty) {
                        localPrefs.edit {
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
                                    }
                                    PreferenceKey.BATTERY_CHARGE_THRESHOLD_KEY -> {
                                        val newValue = dataMap.getInt(key)
                                        putInt(key, newValue)
                                    }
                                }
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    Log.d("PreferenceSyncLayer", "Failed to get current watch prefs")
                }
    }

    fun pushNewData() {
        // Get updated sharedPreferences
        val batterySyncEnabled = localPrefs.getBoolean(PreferenceKey.BATTERY_SYNC_ENABLED_KEY, false)
        val phoneBatteryChargedNoti = localPrefs.getBoolean(PreferenceKey.BATTERY_PHONE_CHARGE_NOTI_KEY, false)
        val watchBatteryChargedNoti = localPrefs.getBoolean(PreferenceKey.BATTERY_WATCH_CHARGE_NOTI_KEY, false)
        val batteryChargeThreshold = localPrefs.getInt(PreferenceKey.BATTERY_CHARGE_THRESHOLD_KEY, 90)
        val interruptFilterSyncToWatch = localPrefs.getBoolean(PreferenceKey.INTERRUPT_FILTER_SYNC_TO_WATCH_KEY, false)
        val interruptFilterSyncToPhone = localPrefs.getBoolean(PreferenceKey.INTERRUPT_FILTER_SYNC_TO_PHONE_KEY, false)
        val interruptFilterSyncWithTheater = localPrefs.getBoolean(PreferenceKey.INTERRUPT_FILTER_ON_WITH_THEATER_KEY, false)
        val lockPhoneEnabled = localPrefs.getBoolean(PreferenceKey.PHONE_LOCKING_ENABLED_KEY, false)

        if (!batterySyncEnabled) {
            localPrefs.edit()
                    .remove(PreferenceKey.BATTERY_PERCENT_KEY)
                    .remove(PreferenceKey.BATTERY_SYNC_LAST_WHEN_KEY).apply()
        }

        // Create updated sharedPreferences object
        val syncedPrefUpdateReq = PutDataMapRequest.create(preferenceChangePath)
        syncedPrefUpdateReq.dataMap.putBoolean(PreferenceKey.BATTERY_SYNC_ENABLED_KEY, batterySyncEnabled)
        syncedPrefUpdateReq.dataMap.putBoolean(PreferenceKey.BATTERY_PHONE_CHARGE_NOTI_KEY, phoneBatteryChargedNoti)
        syncedPrefUpdateReq.dataMap.putBoolean(PreferenceKey.BATTERY_WATCH_CHARGE_NOTI_KEY, watchBatteryChargedNoti)
        syncedPrefUpdateReq.dataMap.putInt(PreferenceKey.BATTERY_CHARGE_THRESHOLD_KEY, batteryChargeThreshold)
        syncedPrefUpdateReq.dataMap.putBoolean(PreferenceKey.INTERRUPT_FILTER_SYNC_TO_WATCH_KEY, interruptFilterSyncToWatch)
        syncedPrefUpdateReq.dataMap.putBoolean(PreferenceKey.INTERRUPT_FILTER_SYNC_TO_PHONE_KEY, interruptFilterSyncToPhone)
        syncedPrefUpdateReq.dataMap.putBoolean(PreferenceKey.INTERRUPT_FILTER_ON_WITH_THEATER_KEY, interruptFilterSyncWithTheater)
        syncedPrefUpdateReq.dataMap.putBoolean(PreferenceKey.PHONE_LOCKING_ENABLED_KEY, lockPhoneEnabled)

        // Send updated sharedPreferences
        syncedPrefUpdateReq.setUrgent()
        dataClient.putDataItem(syncedPrefUpdateReq.asPutDataRequest())
    }

    fun pushNewData(key: String) {
        val syncedPrefUpdateReq = PutDataMapRequest.create(preferenceChangePath)
        when (key) {
            PreferenceKey.PHONE_LOCKING_ENABLED_KEY,
            PreferenceKey.BATTERY_SYNC_ENABLED_KEY,
            PreferenceKey.BATTERY_PHONE_CHARGE_NOTI_KEY,
            PreferenceKey.BATTERY_WATCH_CHARGE_NOTI_KEY,
            PreferenceKey.INTERRUPT_FILTER_SYNC_TO_PHONE_KEY,
            PreferenceKey.INTERRUPT_FILTER_SYNC_TO_WATCH_KEY,
            PreferenceKey.INTERRUPT_FILTER_ON_WITH_THEATER_KEY -> {
                val newValue = localPrefs.getBoolean(key, false)
                syncedPrefUpdateReq.dataMap.putBoolean(key, newValue)
            }
            PreferenceKey.BATTERY_CHARGE_THRESHOLD_KEY -> {
                val newValue = localPrefs.getInt(key, 90)
                syncedPrefUpdateReq.dataMap.putInt(key, newValue)
            }
        }
        if (!syncedPrefUpdateReq.dataMap.isEmpty) {
            syncedPrefUpdateReq.setUrgent()
            dataClient.putDataItem(syncedPrefUpdateReq.asPutDataRequest())
        }
    }

    fun getCurrentData(): Task<DataItem> {
        val uri = PutDataMapRequest.create(preferenceChangePath).uri
        return dataClient.getDataItem(uri)
    }
}
