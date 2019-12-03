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
import android.util.Log
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.room.Room
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.common.References
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable

class WatchConnectionService :
        Service(),
        SharedPreferences.OnSharedPreferenceChangeListener {

    private var watchConnectionListener: CapabilityClient.OnCapabilityChangedListener? = null

    private val binder = WatchConnectionServiceBinder()

    private val watchConnectionInterfaces: ArrayList<WatchConnectionInterface> = ArrayList()
    private val watchPreferenceChangeInterfaces: ArrayList<WatchPreferenceChangeInterface> = ArrayList()

    private lateinit var database: WatchDatabase
    private lateinit var capabilityClient: CapabilityClient
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var dataClient: DataClient
    private lateinit var nodeClient: NodeClient

    private var preferenceChangePath = ""

    private var connectedWatchId: String = ""

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            AUTO_ADD_WATCHES_KEY -> {
                setAutoAddWatches()
            }
        }
    }

    override fun onBind(p0: Intent?): IBinder? = binder

    override fun onCreate() {
        super.onCreate()

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        database = Room.databaseBuilder(applicationContext, WatchDatabase::class.java, "watch-db")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build()

        setConnectedWatchById(sharedPreferences.getString(LAST_CONNECTED_NODE_ID_KEY, "") ?: "")

        capabilityClient = Wearable.getCapabilityClient(this)

        dataClient = Wearable.getDataClient(this)
        nodeClient = Wearable.getNodeClient(this)

        Log.d("WatchConnectionService", "Starting service")

        setAutoAddWatches()
    }

    override fun onDestroy() {
        super.onDestroy()

        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)

        if (database.isOpen) database.close()

        if (watchConnectionListener != null) capabilityClient.removeListener(watchConnectionListener!!)
    }

    private fun setAutoAddWatches() {
        if (sharedPreferences.getBoolean(AUTO_ADD_WATCHES_KEY, false)) {
            if (watchConnectionListener == null) {
                watchConnectionListener = CapabilityClient.OnCapabilityChangedListener { capabilityInfo ->
                    for (node in capabilityInfo.nodes) {
                        ensureWatchRegistered(node)
                    }
                }
            }
            capabilityClient.addListener(watchConnectionListener!!, References.CAPABILITY_WATCH_APP)
            capabilityClient.getCapability(References.CAPABILITY_WATCH_APP, CapabilityClient.FILTER_ALL)
                    .addOnSuccessListener {
                        for (node in it.nodes) {
                            ensureWatchRegistered(node)
                        }
                    }
        } else if (watchConnectionListener != null) {
            capabilityClient.removeListener(watchConnectionListener!!)
        }
    }

    fun getAllRegisteredWatches(): List<Watch> {
        if (database.isOpen) {
            return database.watchDao().getAll()
        }
        return ArrayList()
    }

    fun getAllConnectedWatches(): Task<CapabilityInfo> = capabilityClient.getCapability(References.CAPABILITY_WATCH_APP, CapabilityClient.FILTER_ALL)

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

    fun getConnectedWatchId(): String = connectedWatchId

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

        sharedPreferences.edit {
            putString(LAST_CONNECTED_NODE_ID_KEY, connectedWatchId)
        }

        for (connectionInterface in watchConnectionInterfaces) {
            connectionInterface.onConnectedWatchChanged(true)
        }
    }

    fun forceSyncPreferences(): Task<DataItem>? {
        return forceSyncPreferences(connectedWatchId)
    }

    private fun forceSyncPreferences(watchId: String?): Task<DataItem>? {
        if (!watchId.isNullOrEmpty()) {
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
            val syncedPrefUpdateReq = PutDataMapRequest.create("/preference-change_$watchId")
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

    fun updatePreferenceOnWatch(key: String): Task<DataItem>? {
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

    fun registerWatchPreferenceChangeInterface(watchPreferenceChangeInterface: WatchPreferenceChangeInterface) {
        if (!watchPreferenceChangeInterfaces.contains(watchPreferenceChangeInterface)) {
            watchPreferenceChangeInterfaces.add(watchPreferenceChangeInterface)
        }
    }

    fun unregisterWatchPreferenceChangeInterface(watchPreferenceChangeInterface: WatchPreferenceChangeInterface) {
        if (watchPreferenceChangeInterfaces.contains(watchPreferenceChangeInterface)) {
            watchPreferenceChangeInterfaces.remove(watchPreferenceChangeInterface)
        }
    }

    fun updatePrefInDatabase(key: String, newValue: Any): Boolean {
        return updatePrefInDatabase(connectedWatchId, key, newValue)
    }

    fun updatePrefInDatabase(id: String, key: String, newValue: Any): Boolean {
        when (newValue) {
            is Boolean -> {
                val boolPreference = BoolPreference(id, key, newValue)
                database.boolPreferenceDao().update(boolPreference)
                for (watchPreferenceChangeInterface in watchPreferenceChangeInterfaces) {
                    watchPreferenceChangeInterface.boolPreferenceChanged(boolPreference)
                }
            }
            is Int -> {
                val intPreference = IntPreference(id, key, newValue)
                database.intPreferenceDao().update(intPreference)
                for (watchPreferenceChangeInterface in watchPreferenceChangeInterfaces) {
                    watchPreferenceChangeInterface.intPreferenceChanged(intPreference)
                }
            }
            else -> return false
        }
        return true
    }

    fun updateWatchNickname(watchId: String, nickname: String): Boolean {
        if (database.isOpen) {
            database.watchDao().setWatchName(watchId, nickname)
            return true
        }
        return false
    }

    fun getWatchById(watchId: String?): Watch? {
        if (database.isOpen or !watchId.isNullOrEmpty()) {
            return database.watchDao().findById(watchId!!)
        }
        return null
    }

    fun addWatch(watch: Watch): Boolean {
        if (database.isOpen) {
            database.watchDao().add(watch)
            return true
        }
        return false
    }

    fun forgetWatch(watchId: String?): Boolean {
        if (!watchId.isNullOrEmpty() && database.isOpen) {
            val success = clearPreferencesForWatch(watchId)
            database.watchDao().remove(watchId)
            return success
        }
        return false
    }

    fun clearPreferencesForWatch(watchId: String?): Boolean {
        if (!watchId.isNullOrEmpty() && database.isOpen) {
            database.intPreferenceDao().deleteAllForWatch(watchId)
            database.boolPreferenceDao().deleteAllForWatch(watchId)
            forceSyncPreferences(watchId)
            if (watchId == connectedWatchId) {
                updateLocalPreferences()
            }
            return true
        }
        return false
    }

    fun getBoolPrefsForRegisteredWatches(key: String): Array<BoolPreference>? {
        if (database.isOpen) {
            return database.boolPreferenceDao().getAllForKey(key)
        }
        return null
    }

    private fun deleteLocalPreferences() {
        sharedPreferences.edit {
            remove(PreferenceKey.PHONE_LOCKING_ENABLED_KEY)
            remove(PreferenceKey.BATTERY_SYNC_ENABLED_KEY)
            remove(PreferenceKey.BATTERY_PHONE_CHARGE_NOTI_KEY)
            remove(PreferenceKey.BATTERY_WATCH_CHARGE_NOTI_KEY)
            remove(PreferenceKey.INTERRUPT_FILTER_SYNC_TO_PHONE_KEY)
            remove(PreferenceKey.INTERRUPT_FILTER_SYNC_TO_WATCH_KEY)
            remove(PreferenceKey.INTERRUPT_FILTER_ON_WITH_THEATER_KEY)
            remove(PreferenceKey.BATTERY_CHARGE_THRESHOLD_KEY)
        }
    }

    private fun updateLocalPreferences() {
        val watch = getConnectedWatch() ?: return
        deleteLocalPreferences()
        sharedPreferences.edit {
            watch.boolPrefs.forEach { (key, value) ->
                putBoolean(key, value)
            }
            watch.intPrefs.forEach { (key, value) ->
                putInt(key, value)
            }
        }
    }

    fun getWatchByBatterySyncJobId(batterySyncJobID: Int): Watch? {
        if (database.isOpen) {
            return database.watchDao().findByBatterySyncJobId(batterySyncJobID)
        }
        return null
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
        abstract fun onWatchManagerBound(service: WatchConnectionService)
        abstract fun onWatchManagerUnbound()

        override fun onServiceConnected(componentName: ComponentName?, binder: IBinder?) {
            val service = (binder as WatchConnectionServiceBinder).getService()
            onWatchManagerBound(service)
        }

        override fun onServiceDisconnected(componentName: ComponentName?) {
            onWatchManagerUnbound()
        }
    }
}
