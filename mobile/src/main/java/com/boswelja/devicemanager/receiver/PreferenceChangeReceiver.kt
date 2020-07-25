/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.receiver

import android.content.Intent
import com.boswelja.devicemanager.batterysync.BatterySyncWorker
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.dndsync.DnDLocalChangeService
import com.boswelja.devicemanager.watchmanager.WatchManager
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class PreferenceChangeReceiver : WearableListenerService() {

    private val coroutineScope = MainScope()

    private val watchManager: WatchManager by lazy { WatchManager.get(this) }

    override fun onDataChanged(dataEvents: DataEventBuffer?) {
        Timber.i("onDataChanged() called")
        if (dataEvents != null) {
            Timber.i("Handling preference change")
            for (event in dataEvents) {
                val senderId = event.dataItem.uri.host!!
                handleWatchPreferenceChange(senderId, event)
            }
        }
    }

    /**
     * Starts or stops the [BatterySyncWorker] as needed.
     * @param batterySyncEnabled Whether battery sync is enabled.
     * @param watchId The watch ID to start/stop the worker for.
     */
    private suspend fun updateBatterySyncWorker(batterySyncEnabled: Boolean, watchId: String) {
        withContext(Dispatchers.IO) {
            if (batterySyncEnabled) {
                Timber.i("Starting BatterySyncWorker")
                BatterySyncWorker.startWorker(this@PreferenceChangeReceiver, watchId)
            } else {
                Timber.i("Stopping BatterySyncWorker")
                BatterySyncWorker.stopWorker(this@PreferenceChangeReceiver, watchId)
            }
        }
    }

    /**
     * Starts the [DnDLocalChangeService] if needed.
     */
    private fun updateDnDSyncToWatch(dndSyncEnabled: Boolean) {
        if (dndSyncEnabled) {
            Intent(this, DnDLocalChangeService::class.java).also {
                Timber.i("Starting DnDLocalChangeService")
                Compat.startForegroundService(this, it)
            }
        }
    }

    /**
     * Determine which preferences have changed from a given [DataEvent] and handle them.
     * @param event The [DataEvent] that holds a change in preferences.
     */
    private fun handleWatchPreferenceChange(senderId: String, event: DataEvent) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                if (!dataMap.isEmpty) {
                    for (key in dataMap.keySet()) {
                        when (key) {
                            PreferenceKey.PHONE_LOCKING_ENABLED_KEY,
                            PreferenceKey.BATTERY_PHONE_CHARGE_NOTI_KEY,
                            PreferenceKey.BATTERY_WATCH_CHARGE_NOTI_KEY,
                            PreferenceKey.DND_SYNC_TO_PHONE_KEY,
                            PreferenceKey.DND_SYNC_WITH_THEATER_KEY
                            -> {
                                val newValue = dataMap.getBoolean(key)
                                watchManager.updatePreferenceInDatabase(senderId, key, newValue)
                            }
                            PreferenceKey.DND_SYNC_TO_WATCH_KEY -> {
                                val newValue = dataMap.getBoolean(key)
                                watchManager.updatePreferenceInDatabase(senderId, key, newValue)
                                updateDnDSyncToWatch(newValue)
                            }
                            PreferenceKey.BATTERY_SYNC_ENABLED_KEY -> {
                                val newValue = dataMap.getBoolean(key)
                                watchManager.updatePreferenceInDatabase(senderId, key, newValue)
                                updateBatterySyncWorker(newValue, senderId)
                            }
                            PreferenceKey.BATTERY_CHARGE_THRESHOLD_KEY -> {
                                val newValue = dataMap.getInt(key)
                                watchManager.updatePreferenceInDatabase(senderId, key, newValue)
                            }
                        }
                    }
                }
            } catch (e: IllegalArgumentException) {
                Timber.e(e)
            }
        }
    }
}
