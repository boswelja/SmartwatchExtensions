/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.watchconnectionmanager

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.os.Binder
import android.os.IBinder
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.room.Room
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.common.References
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable

class WatchConnectionService : Service() {

    private var watchConnectionListener: CapabilityClient.OnCapabilityChangedListener? = null

    private val binder = WatchConnectionServiceBinder()

    private val watchConnectionInterfaces: ArrayList<WatchConnectionInterface> = ArrayList()

    private lateinit var database: WatchDatabase
    private lateinit var capabilityClient: CapabilityClient
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var dataClient: DataClient

    private var preferenceChangePath = ""

    private var connectedWatchId: String = ""

    override fun onBind(p0: Intent?): IBinder? = binder

    override fun onCreate() {
        super.onCreate()

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        database = Room.databaseBuilder(applicationContext, WatchDatabase::class.java, "watch-db")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build()

        setConnectedWatchById(sharedPreferences.getString(LAST_CONNECTED_NODE_ID_KEY, "") ?: "")

        capabilityClient = Wearable.getCapabilityClient(this)

        dataClient = Wearable.getDataClient(this)

        if (sharedPreferences.getBoolean(AUTO_ADD_WATCHES_KEY, false)) {
            watchConnectionListener = CapabilityClient.OnCapabilityChangedListener { capabilityInfo ->
                for (node in capabilityInfo.nodes) {
                    ensureWatchRegistered(node)
                }
            }
            capabilityClient.addListener(watchConnectionListener!!, References.CAPABILITY_WATCH_APP)
            capabilityClient.getCapability(References.CAPABILITY_WATCH_APP, CapabilityClient.FILTER_ALL)
                    .addOnSuccessListener {
                        for (node in it.nodes) {
                            ensureWatchRegistered(node)
                        }
                    }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (database.isOpen) database.close()

        if (watchConnectionListener != null) capabilityClient.removeListener(watchConnectionListener!!)

        sharedPreferences.edit {
            putString(LAST_CONNECTED_NODE_ID_KEY, connectedWatchId)
        }
    }

    fun getAllWatches(): List<Watch>? {
        if (database.isOpen) {
            return database.watchDao().getAll()
        }
        return null
    }

    fun getConnectedWatch(): Watch? {
        val watch = database.watchDao().findById(connectedWatchId)
        if (watch != null) {
            for (intPreference in database.intPreferenceDao().getAllForWatch(watch.id)) {
                watch.intPrefs[intPreference.key] = intPreference.value
            }
            for (boolPreference in database.boolPreferenceDao().getAllForWatch(watch.id)) {
                watch.boolPrefs[boolPreference.key] = boolPreference.value
            }
        }
        return watch
    }

    fun setConnectedWatchById(id: String) {
        for (connectionInterface in watchConnectionInterfaces) {
            connectionInterface.onConnectedWatchChanging()
        }

        if (database.watchDao().findById(id) == null) {
            for (connectionInterface in watchConnectionInterfaces) {
                connectionInterface.onConnectedWatchChanged(false)
            }
            return
        }
        connectedWatchId = id
        preferenceChangePath = "/preference-change_$connectedWatchId"

        updateLocalPreferences()

        for (connectionInterface in watchConnectionInterfaces) {
            connectionInterface.onConnectedWatchChanged(true)
        }
    }

    fun forceSyncPreferences(): Task<DataItem>? {
        if (connectedWatchId.isNotEmpty()) {
            // Get updated sharedPreferences
            val batterySyncEnabled = sharedPreferences.getBoolean(PreferenceKey.BATTERY_SYNC_ENABLED_KEY, false)
            val phoneBatteryChargedNoti = sharedPreferences.getBoolean(PreferenceKey.BATTERY_PHONE_CHARGE_NOTI_KEY, false)
            val watchBatteryChargedNoti = sharedPreferences.getBoolean(PreferenceKey.BATTERY_WATCH_CHARGE_NOTI_KEY, false)
            val batteryChargeThreshold = sharedPreferences.getInt(PreferenceKey.BATTERY_CHARGE_THRESHOLD_KEY, 90)
            val interruptFilterSyncToWatch = sharedPreferences.getBoolean(PreferenceKey.INTERRUPT_FILTER_SYNC_TO_WATCH_KEY, false)
            val interruptFilterSyncToPhone = sharedPreferences.getBoolean(PreferenceKey.INTERRUPT_FILTER_SYNC_TO_PHONE_KEY, false)
            val interruptFilterSyncWithTheater = sharedPreferences.getBoolean(PreferenceKey.INTERRUPT_FILTER_ON_WITH_THEATER_KEY, false)
            val lockPhoneEnabled = sharedPreferences.getBoolean(PreferenceKey.PHONE_LOCKING_ENABLED_KEY, false)

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

    fun updatePreference(key: String): Task<DataItem>? {
        if (connectedWatchId.isNotEmpty()) {
            val connectedWatch = getConnectedWatch()!!
            val syncedPrefUpdateReq = PutDataMapRequest.create(preferenceChangePath)
            when (key) {
                PreferenceKey.PHONE_LOCKING_ENABLED_KEY,
                PreferenceKey.BATTERY_SYNC_ENABLED_KEY,
                PreferenceKey.BATTERY_PHONE_CHARGE_NOTI_KEY,
                PreferenceKey.BATTERY_WATCH_CHARGE_NOTI_KEY,
                PreferenceKey.INTERRUPT_FILTER_SYNC_TO_PHONE_KEY,
                PreferenceKey.INTERRUPT_FILTER_SYNC_TO_WATCH_KEY,
                PreferenceKey.INTERRUPT_FILTER_ON_WITH_THEATER_KEY -> {
                    val newValue = sharedPreferences.getBoolean(key, false)
                    syncedPrefUpdateReq.dataMap.putBoolean(key, newValue)
                    connectedWatch.boolPrefs[key] = newValue
                    database.boolPreferenceDao().update(BoolPreference(connectedWatchId, key, newValue))
                }
                PreferenceKey.BATTERY_CHARGE_THRESHOLD_KEY -> {
                    val newValue = sharedPreferences.getInt(key, 90)
                    syncedPrefUpdateReq.dataMap.putInt(key, newValue)
                    connectedWatch.intPrefs[key] = newValue
                    database.intPreferenceDao().update(IntPreference(connectedWatchId, key, newValue))
                }
            }
            if (!syncedPrefUpdateReq.dataMap.isEmpty) {
                syncedPrefUpdateReq.setUrgent()
                return dataClient.putDataItem(syncedPrefUpdateReq.asPutDataRequest())
            }
        }
        return null
    }

    fun registerWatchConnectionInterface(connectionInterface: WatchConnectionInterface) {
        if (!watchConnectionInterfaces.contains(connectionInterface)) {
            watchConnectionInterfaces.add(connectionInterface)
        }
    }

    fun unregisterWatchConnectionInterface(connectionInterface: WatchConnectionInterface) {
        if (watchConnectionInterfaces.contains(connectionInterface)) {
            watchConnectionInterfaces.remove(connectionInterface)
        }
    }

    fun updatePrefInDatabase(key: String, newValue: Any): Boolean {
        return updatePrefInDatabase(connectedWatchId, key, newValue)
    }

    fun updatePrefInDatabase(id: String, key: String, newValue: Any): Boolean {
        when (newValue) {
            is Boolean -> {
                database.boolPreferenceDao().update(BoolPreference(id, key, newValue))
            }
            is Int -> {
                database.intPreferenceDao().update(IntPreference(id, key, newValue))
            }
            else -> return false
        }
        return true
    }

    private fun updateLocalPreferences() {
        val watch = getConnectedWatch() ?: return
        sharedPreferences.edit {
            watch.boolPrefs.forEach { (key, value) ->
                putBoolean(key, value)
            }
            watch.intPrefs.forEach { (key, value) ->
                putInt(key, value)
            }
        }
    }

    private fun ensureWatchRegistered(node: Node) {
        if (database.watchDao().findById(node.id) == null) {
            val newWatch = Watch(node)
            database.watchDao().add(newWatch)
            for (connectionInterface in watchConnectionInterfaces) {
                connectionInterface.onWatchAdded(newWatch)
            }
        }
    }

    companion object {
        private const val LAST_CONNECTED_NODE_ID_KEY = "last_connected_id"

        private const val AUTO_ADD_WATCHES_KEY = "auto_add_watches"

        fun bind(context: Context, serviceConnection: Connection) {
            context.bindService(Intent(context, WatchConnectionService::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    inner class WatchConnectionServiceBinder : Binder() {
        fun getService(): WatchConnectionService =
                this@WatchConnectionService
    }

    abstract class Connection : ServiceConnection {
        abstract fun onPreferenceSyncServiceBound(service: WatchConnectionService)
        abstract fun onPreferenceSyncServiceUnbound()

        override fun onServiceConnected(componentName: ComponentName?, binder: IBinder?) {
            val service = (binder as WatchConnectionServiceBinder).getService()
            onPreferenceSyncServiceBound(service)
        }

        override fun onServiceDisconnected(componentName: ComponentName?) {
            onPreferenceSyncServiceUnbound()
        }
    }
}
