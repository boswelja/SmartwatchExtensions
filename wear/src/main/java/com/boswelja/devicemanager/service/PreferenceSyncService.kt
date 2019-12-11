/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.service

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.os.Binder
import android.os.IBinder
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.common.References
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable

class PreferenceSyncService : Service() {

    private val binder: PreferenceSyncBinder = PreferenceSyncBinder()

    private var nodeId: String = ""
    private val preferenceChangePath = "/preference_change"

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var dataClient: DataClient

    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        dataClient = Wearable.getDataClient(this)

        Wearable.getCapabilityClient(this)
                .getCapability(References.CAPABILITY_PHONE_APP, CapabilityClient.FILTER_ALL)
                .addOnSuccessListener {
                    nodeId = (it.nodes.firstOrNull { n -> n.isNearby } ?: it.nodes.firstOrNull())?.id ?: ""
                }
    }

    fun pushNewData(key: String): Task<DataItem>? {
        if (nodeId.isNotEmpty()) {
            val syncedPrefUpdateReq = PutDataMapRequest.create(preferenceChangePath)
            when (key) {
                PreferenceKey.PHONE_LOCKING_ENABLED_KEY,
                PreferenceKey.BATTERY_SYNC_ENABLED_KEY,
                PreferenceKey.BATTERY_PHONE_CHARGE_NOTI_KEY,
                PreferenceKey.BATTERY_WATCH_CHARGE_NOTI_KEY,
                PreferenceKey.DND_SYNC_TO_PHONE_KEY,
                PreferenceKey.DND_SYNC_TO_WATCH_KEY,
                PreferenceKey.DND_SYNC_WITH_THEATER_KEY -> {
                    val newValue = sharedPreferences.getBoolean(key, false)
                    syncedPrefUpdateReq.dataMap.putBoolean(key, newValue)
                }
                PreferenceKey.BATTERY_CHARGE_THRESHOLD_KEY -> {
                    val newValue = sharedPreferences.getInt(key, 90)
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
}
