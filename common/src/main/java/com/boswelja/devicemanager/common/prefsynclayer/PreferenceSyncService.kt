/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.common.prefsynclayer

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.common.PreferenceKey
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable

class PreferenceSyncService : Service() {

    private val binder: PreferenceSyncBinder = PreferenceSyncBinder()
    private val preferenceSyncListeners: ArrayList<PreferenceSyncListener> = ArrayList()

    private var nodeId: String = ""
    private var preferenceChangePath = ""

    private lateinit var localPrefs: SharedPreferences
    private lateinit var dataClient: DataClient

    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        localPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        dataClient = Wearable.getDataClient(this)
        Log.d("PreferenceSyncService", "Service starting")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("PreferenceSyncService", "Service stopping")
    }

    private fun updatePreferenceChangePath() {
        preferenceChangePath = "/preference-change_$nodeId"
    }

    fun registerPreferenceSyncListener(preferenceSyncListener: PreferenceSyncListener): Boolean {
        if (!preferenceSyncListeners.contains(preferenceSyncListener)) {
            preferenceSyncListeners.add(preferenceSyncListener)
            return true
        }
        return false
    }

    fun unregisterPreferenceSyncListener(preferenceSyncListener: PreferenceSyncListener): Boolean {
        if (preferenceSyncListeners.contains(preferenceSyncListener)) {
            preferenceSyncListeners.remove(preferenceSyncListener)
            return true
        }
        return false
    }

    fun pushNewData(): Task<DataItem>? {
        if (nodeId.isNotEmpty()) {
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
            return dataClient.putDataItem(syncedPrefUpdateReq.asPutDataRequest())
        }
        return null
    }

    fun pushNewData(key: String): Task<DataItem>? {
        if (nodeId.isNotEmpty()) {
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
                return dataClient.putDataItem(syncedPrefUpdateReq.asPutDataRequest())
            }
        }
        return null
    }

    fun setConnectedNodeId(nodeId: String) {
        for (preferenceSyncListener in preferenceSyncListeners) {
            preferenceSyncListener.onConnectedNodeChanging()
        }
        this.nodeId = nodeId
        updatePreferenceChangePath()
        updateLocalPreferences()
    }

    fun updateLocalPreferences() {
        val uri = PutDataMapRequest.create(preferenceChangePath).uri
        dataClient.getDataItem(uri)
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
                                        for (preferenceSyncListener in preferenceSyncListeners) {
                                            preferenceSyncListener.onLocalPreferenceUpdated(key)
                                        }
                                    }
                                    PreferenceKey.BATTERY_CHARGE_THRESHOLD_KEY -> {
                                        val newValue = dataMap.getInt(key)
                                        putInt(key, newValue)
                                        for (preferenceSyncListener in preferenceSyncListeners) {
                                            preferenceSyncListener.onLocalPreferenceUpdated(key)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    for (preferenceSyncListener in preferenceSyncListeners) {
                        preferenceSyncListener.onConnectedNodeChanged(true)
                    }
                }
                .addOnFailureListener {
                    for (preferenceSyncListener in preferenceSyncListeners) {
                        preferenceSyncListener.onConnectedNodeChanged(false)
                    }
                }
    }

    private inner class PreferenceSyncBinder : Binder() {
        fun getService(): PreferenceSyncService {
            return this@PreferenceSyncService
        }
    }

    abstract class PreferenceSyncServiceConnection : ServiceConnection {
        abstract fun onPreferenceSyncServiceBound(preferenceSyncService: PreferenceSyncService)
        abstract fun onPreferenceSyncServiceUnbound()

        override fun onServiceConnected(componentName: ComponentName?, binder: IBinder?) {
            val service = (binder as PreferenceSyncBinder).getService()
            onPreferenceSyncServiceBound(service)
        }

        override fun onServiceDisconnected(componentName: ComponentName?) {
            onPreferenceSyncServiceUnbound()
        }
    }

    interface PreferenceSyncListener {
        fun onConnectedNodeChanging()
        fun onConnectedNodeChanged(success: Boolean)
        fun onLocalPreferenceUpdated(preferenceKey: String)
    }
}
