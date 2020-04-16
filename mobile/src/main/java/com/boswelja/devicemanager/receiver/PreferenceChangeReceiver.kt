/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
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
import com.boswelja.devicemanager.batterysync.BatterySyncWorker
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.dndsync.DnDLocalChangeService
import com.boswelja.devicemanager.watchconnectionmanager.WatchConnectionService
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import timber.log.Timber

class PreferenceChangeReceiver : WearableListenerService() {

    private val coroutineScope = MainScope()

    private var watchConnectionManager: WatchConnectionService? = null
    private var sharedPreferences: SharedPreferences? = null

    private val watchConnManConnection = object : WatchConnectionService.Connection() {
        override fun onWatchManagerBound(service: WatchConnectionService) {
            Timber.i("onWatchManagerBound() called")
            watchConnectionManager = service
        }

        override fun onWatchManagerUnbound() {
            Timber.w("onWatchManagerUnbound() called")
            watchConnectionManager = null
        }
    }

    override fun onCreate() {
        super.onCreate()
        Timber.i("onCreate() called")
        WatchConnectionService.bind(this, watchConnManConnection)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(watchConnManConnection)
    }

    override fun onDataChanged(dataEvents: DataEventBuffer?) {
        Timber.i("onDataChanged() called")
        if (dataEvents != null) {
            Timber.i("Handling preference change")
            val selectedWatchId = watchConnectionManager?.getConnectedWatch()?.id
            for (event in dataEvents) {
                val senderId = event.dataItem.uri.host!!
                if (!selectedWatchId.isNullOrEmpty() && senderId == selectedWatchId) {
                    handleSelectedWatchPreferenceChange(event)
                } else {
                    handleOtherWatchPreferenceChange(senderId, event)
                }
            }
        }
    }

    /**
     * Handle a preference change for the watch that's currently selected in [WatchConnectionService].
     * @param key The key of the changed preference.
     * @param newValue The new value of the preference.
     */
    private fun onPreferenceChanged(key: String, newValue: Any) {
        Timber.i("onPreferenceChanged($key, $newValue) called")
        when (key) {
            PreferenceKey.BATTERY_SYNC_ENABLED_KEY -> {
                coroutineScope.launch(Dispatchers.IO) {
                    if (newValue == true) {
                        Timber.i("Starting BatterySyncWorker")
                        BatterySyncWorker.startWorker(watchConnectionManager)
                    } else {
                        Timber.i("Stopping BatterySyncWorker")
                        BatterySyncWorker.stopWorker(watchConnectionManager)
                    }
                }
            }
            PreferenceKey.DND_SYNC_TO_WATCH_KEY -> {
                if (newValue == true) {
                    Intent(this, DnDLocalChangeService::class.java).also {
                        Timber.i("Starting DnDLocalChangeService")
                        Compat.startForegroundService(this, it)
                    }
                }
            }
        }
        coroutineScope.launch(Dispatchers.IO) {
            Timber.i("Updating preference in database")
            watchConnectionManager?.updatePrefInDatabase(key, newValue)
        }
    }

    /**
     * Determine which preferences have changed from a given [DataEvent] and handle them for the
     * currently selected watch.
     * @param event The [DataEvent] that holds a change in preferences.
     */
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

    /**
     * Determine which preferences have changed from a given [DataEvent] and handle them.
     * @param event The [DataEvent] that holds a change in preferences.
     */
    private fun handleOtherWatchPreferenceChange(senderId: String, event: DataEvent) {
        coroutineScope.launch(Dispatchers.IO) {
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
                            watchConnectionManager?.updatePrefInDatabase(senderId, key, newValue)
                        }
                        PreferenceKey.BATTERY_CHARGE_THRESHOLD_KEY -> {
                            val newValue = dataMap.getInt(key)
                            watchConnectionManager?.updatePrefInDatabase(senderId, key, newValue)
                        }
                    }
                }
            }
        }
    }
}
