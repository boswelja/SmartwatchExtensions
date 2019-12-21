/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.receiver

import android.content.Intent
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.batterysync.BatterySyncJob
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.dndsync.DnDLocalChangeService
import com.boswelja.devicemanager.watchconnectionmanager.WatchConnectionService
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class PreferenceChangeReceiver : WearableListenerService() {

    private val coroutineScope = MainScope()

    private var watchConnectionManager: WatchConnectionService? = null
    private var sharedPreferences: SharedPreferences? = null

    private val watchConnManConnection = object : WatchConnectionService.Connection() {
        override fun onWatchManagerBound(service: WatchConnectionService) {
            watchConnectionManager = service
        }

        override fun onWatchManagerUnbound() {
            watchConnectionManager = null
        }
    }

    override fun onCreate() {
        super.onCreate()
        WatchConnectionService.bind(this, watchConnManConnection)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(watchConnManConnection)
    }

    override fun onDataChanged(dataEvents: DataEventBuffer?) {
        if (dataEvents != null) {
            coroutineScope.launch {
                val selectedWatchId = watchConnectionManager?.getConnectedWatch()?.id
                if (!selectedWatchId.isNullOrEmpty()) {
                    for (event in dataEvents) {
                        val senderId = event.dataItem.uri.host!!
                        if (senderId == selectedWatchId) {
                            handleSelectedWatchPreferenceChange(event)
                        } else {
                            handleOtherWatchPreferenceChange(senderId, event)
                        }
                    }
                }
            }
        }
    }

    private fun onPreferenceChanged(key: String, newValue: Any) {
        when (key) {
            PreferenceKey.BATTERY_SYNC_ENABLED_KEY -> {
                coroutineScope.launch {
                    if (newValue == true) {
                        BatterySyncJob.startJob(watchConnectionManager)
                    } else {
                        BatterySyncJob.stopJob(watchConnectionManager)
                    }
                }
            }
            PreferenceKey.DND_SYNC_TO_WATCH_KEY -> {
                if (newValue == true) {
                    Intent(this, DnDLocalChangeService::class.java).also {
                        Compat.startForegroundService(this, it)
                    }
                }
            }
        }
        coroutineScope.launch {
            watchConnectionManager?.updatePrefInDatabase(key, newValue)
        }
    }

    private fun handleSelectedWatchPreferenceChange(event: DataEvent) {
        val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
        if (!dataMap.isEmpty) {
            if (sharedPreferences == null) sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            sharedPreferences!!.edit {
                for (key in dataMap.keySet()) {
                    when (key) {
                        PreferenceKey.PHONE_LOCKING_ENABLED_KEY,
                        PreferenceKey.BATTERY_SYNC_ENABLED_KEY,
                        PreferenceKey.BATTERY_PHONE_CHARGE_NOTI_KEY,
                        PreferenceKey.BATTERY_WATCH_CHARGE_NOTI_KEY,
                        PreferenceKey.DND_SYNC_TO_WATCH_KEY,
                        PreferenceKey.DND_SYNC_TO_PHONE_KEY,
                        PreferenceKey.DND_SYNC_WITH_THEATER_KEY -> {
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

    private fun handleOtherWatchPreferenceChange(senderId: String, event: DataEvent) {
        val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
        if (!dataMap.isEmpty) {
            for (key in dataMap.keySet()) {
                when (key) {
                    PreferenceKey.PHONE_LOCKING_ENABLED_KEY,
                    PreferenceKey.BATTERY_SYNC_ENABLED_KEY,
                    PreferenceKey.BATTERY_PHONE_CHARGE_NOTI_KEY,
                    PreferenceKey.BATTERY_WATCH_CHARGE_NOTI_KEY,
                    PreferenceKey.DND_SYNC_TO_WATCH_KEY,
                    PreferenceKey.DND_SYNC_TO_PHONE_KEY,
                    PreferenceKey.DND_SYNC_WITH_THEATER_KEY -> {
                        val newValue = dataMap.getBoolean(key)
                        coroutineScope.launch {
                            watchConnectionManager?.updatePrefInDatabase(senderId, key, newValue)
                        }
                    }
                    PreferenceKey.BATTERY_CHARGE_THRESHOLD_KEY -> {
                        val newValue = dataMap.getInt(key)
                        coroutineScope.launch {
                            watchConnectionManager?.updatePrefInDatabase(senderId, key, newValue)
                        }
                    }
                }
            }
        }
    }
}
