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
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WatchConnectionService :
        Service(),
        SharedPreferences.OnSharedPreferenceChangeListener {

    private var watchConnectionListener: CapabilityClient.OnCapabilityChangedListener? = null

    private val binder = WatchConnectionServiceBinder()

    private val watchConnectionInterfaces: ArrayList<WatchConnectionInterface> = ArrayList()
    private val watchPreferenceChangeInterfaces: ArrayList<WatchPreferenceChangeInterface> = ArrayList()
    private val coroutineScope = MainScope()

    private lateinit var database: WatchDatabase
    private lateinit var capabilityClient: CapabilityClient
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var dataClient: DataClient
    private lateinit var nodeClient: NodeClient

    private var connectedWatch: Watch? = null

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
                .build()

        setConnectedWatchById(sharedPreferences.getString(LAST_CONNECTED_NODE_ID_KEY, "") ?: "")

        capabilityClient = Wearable.getCapabilityClient(this)

        dataClient = Wearable.getDataClient(this)
        nodeClient = Wearable.getNodeClient(this)

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
            capabilityClient.getCapability(References.CAPABILITY_WATCH_APP, CapabilityClient.FILTER_REACHABLE)
                    .addOnSuccessListener {
                        for (node in it.nodes) {
                            ensureWatchRegistered(node)
                        }
                    }
        } else if (watchConnectionListener != null) {
            capabilityClient.removeListener(watchConnectionListener!!)
        }
    }

    suspend fun getAvailableWatches(): List<Watch> {
        val availableWatches = ArrayList<Watch>()
        withContext(Dispatchers.IO) {
            val connectedNodes = Tasks.await(nodeClient.connectedNodes)
            val registeredWatches = getRegisteredWatches()
            val capableWatches = Tasks.await(capabilityClient.getCapability(References.CAPABILITY_WATCH_APP, CapabilityClient.FILTER_REACHABLE)).nodes
            withContext(Dispatchers.Default) {
                for (node in connectedNodes) {
                    if (!registeredWatches.any { it.id == node.id }) {
                        availableWatches.add(Watch(node, capableWatches.any { it.id == node.id }))
                    }
                }
            }
        }
        return availableWatches
    }

    suspend fun getRegisteredWatches(): List<Watch> {
        val registeredWatches = ArrayList<Watch>()
        try {
            withContext(Dispatchers.IO) {
                val databaseWatches = database.watchDao().getAll()
                val connectedNodes = Tasks.await(nodeClient.connectedNodes)
                val capableNodes = Tasks.await(capabilityClient.getCapability(References.CAPABILITY_WATCH_APP, CapabilityClient.FILTER_ALL)).nodes
                withContext(Dispatchers.Default) {
                    for (watch in databaseWatches) {
                        registeredWatches.add(Watch(watch.id, watch.name, watch.batterySyncJobId, capableNodes.any { it.id == watch.id }, connectedNodes.any { it.id == watch.id }))
                    }
                }
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
        return registeredWatches
    }

    suspend fun getWatchById(watchId: String?): Watch? {
        var watch: Watch? = null
        try {
            withContext(Dispatchers.IO) {
                if (!watchId.isNullOrEmpty()) {
                    val databaseWatch = database.watchDao().findById(watchId)
                    if (databaseWatch != null) {
                        val boolPrefs = database.boolPreferenceDao().getAllForWatch(databaseWatch.id)
                        val intPrefs = database.intPreferenceDao().getAllForWatch(databaseWatch.id)
                        val connectedNodes = Tasks.await(nodeClient.connectedNodes)
                        val capableNodes = Tasks.await(capabilityClient.getCapability(References.CAPABILITY_WATCH_APP, CapabilityClient.FILTER_ALL)).nodes
                        withContext(Dispatchers.Default) {
                            watch = Watch(databaseWatch.id, databaseWatch.name, databaseWatch.batterySyncJobId, capableNodes.any { it.id == databaseWatch.id }, connectedNodes.any { it.id == databaseWatch.id })
                            for (intPreference in intPrefs) {
                                watch!!.intPrefs[intPreference.key] = intPreference.value
                            }
                            for (boolPreference in boolPrefs) {
                                watch!!.boolPrefs[boolPreference.key] = boolPreference.value
                            }
                        }
                    }
                }
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
        return watch
    }

    fun getConnectedWatch(): Watch? = connectedWatch

    fun getConnectedWatchId(): String? = connectedWatch?.id

    fun setConnectedWatchById(id: String) {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                for (connectionInterface in watchConnectionInterfaces) {
                    connectionInterface.onConnectedWatchChanging()
                }

                if (database.watchDao().findById(id) == null) {
                    withContext(Dispatchers.Main) {
                        for (connectionInterface in watchConnectionInterfaces) {
                            connectionInterface.onConnectedWatchChanged(false)
                        }
                    }
                    return@withContext
                }

                connectedWatch = getWatchById(id)

                updateLocalPreferences()

                sharedPreferences.edit {
                    putString(LAST_CONNECTED_NODE_ID_KEY, id)
                }

                withContext(Dispatchers.Main) {
                    for (connectionInterface in watchConnectionInterfaces) {
                        connectionInterface.onConnectedWatchChanged(true)
                    }
                }
            }
        }
    }

    fun forceSyncPreferences(): Task<DataItem>? {
        return forceSyncPreferences(connectedWatch?.id)
    }

    private fun forceSyncPreferences(watchId: String?): Task<DataItem>? {
        if (!watchId.isNullOrEmpty()) {
            // Get updated sharedPreferences
            val batterySyncEnabled = sharedPreferences.getBoolean(PreferenceKey.BATTERY_SYNC_ENABLED_KEY, false)
            val phoneBatteryChargedNoti = sharedPreferences.getBoolean(PreferenceKey.BATTERY_PHONE_CHARGE_NOTI_KEY, false)
            val watchBatteryChargedNoti = sharedPreferences.getBoolean(PreferenceKey.BATTERY_WATCH_CHARGE_NOTI_KEY, false)
            val batteryChargeThreshold = sharedPreferences.getInt(PreferenceKey.BATTERY_CHARGE_THRESHOLD_KEY, 90)
            val interruptFilterSyncToWatch = sharedPreferences.getBoolean(PreferenceKey.DND_SYNC_TO_WATCH_KEY, false)
            val interruptFilterSyncToPhone = sharedPreferences.getBoolean(PreferenceKey.DND_SYNC_TO_PHONE_KEY, false)
            val interruptFilterSyncWithTheater = sharedPreferences.getBoolean(PreferenceKey.DND_SYNC_WITH_THEATER_KEY, false)
            val lockPhoneEnabled = sharedPreferences.getBoolean(PreferenceKey.PHONE_LOCKING_ENABLED_KEY, false)

            // Create updated sharedPreferences object
            val syncedPrefUpdateReq = PutDataMapRequest.create("/preference-change_$watchId")
            syncedPrefUpdateReq.dataMap.putBoolean(PreferenceKey.BATTERY_SYNC_ENABLED_KEY, batterySyncEnabled)
            syncedPrefUpdateReq.dataMap.putBoolean(PreferenceKey.BATTERY_PHONE_CHARGE_NOTI_KEY, phoneBatteryChargedNoti)
            syncedPrefUpdateReq.dataMap.putBoolean(PreferenceKey.BATTERY_WATCH_CHARGE_NOTI_KEY, watchBatteryChargedNoti)
            syncedPrefUpdateReq.dataMap.putInt(PreferenceKey.BATTERY_CHARGE_THRESHOLD_KEY, batteryChargeThreshold)
            syncedPrefUpdateReq.dataMap.putBoolean(PreferenceKey.DND_SYNC_TO_WATCH_KEY, interruptFilterSyncToWatch)
            syncedPrefUpdateReq.dataMap.putBoolean(PreferenceKey.DND_SYNC_TO_PHONE_KEY, interruptFilterSyncToPhone)
            syncedPrefUpdateReq.dataMap.putBoolean(PreferenceKey.DND_SYNC_WITH_THEATER_KEY, interruptFilterSyncWithTheater)
            syncedPrefUpdateReq.dataMap.putBoolean(PreferenceKey.PHONE_LOCKING_ENABLED_KEY, lockPhoneEnabled)

            // Send updated sharedPreferences
            syncedPrefUpdateReq.setUrgent()
            return dataClient.putDataItem(syncedPrefUpdateReq.asPutDataRequest())
        }
        return null
    }

    suspend fun updatePreferenceOnWatch(key: String): Task<DataItem>? {
        return withContext(Dispatchers.IO) {
            val watchId = connectedWatch?.id
            if (!watchId.isNullOrEmpty()) {
                val connectedWatch = getConnectedWatch()!!
                val syncedPrefUpdateReq = PutDataMapRequest.create("/preference-change_$watchId")
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
                        connectedWatch.boolPrefs[key] = newValue
                        database.boolPreferenceDao().update(BoolPreference(watchId, key, newValue))
                    }
                    PreferenceKey.BATTERY_CHARGE_THRESHOLD_KEY -> {
                        val newValue = sharedPreferences.getInt(key, 90)
                        syncedPrefUpdateReq.dataMap.putInt(key, newValue)
                        connectedWatch.intPrefs[key] = newValue
                        database.intPreferenceDao().update(IntPreference(watchId, key, newValue))
                    }
                }
                if (!syncedPrefUpdateReq.dataMap.isEmpty) {
                    syncedPrefUpdateReq.setUrgent()
                    return@withContext dataClient.putDataItem(syncedPrefUpdateReq.asPutDataRequest())
                }
            }
            return@withContext null
        }
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

    suspend fun updatePrefInDatabase(key: String, newValue: Any): Boolean {
        return updatePrefInDatabase(connectedWatch?.id!!, key, newValue)
    }

    suspend fun updatePrefInDatabase(id: String, key: String, newValue: Any): Boolean {
        if (database.isOpen) {
            return when (newValue) {
                is Boolean -> {
                    withContext(Dispatchers.IO) {
                        val boolPreference = BoolPreference(id, key, newValue)
                        database.boolPreferenceDao().update(boolPreference)
                        withContext(Dispatchers.Main) {
                            for (watchPreferenceChangeInterface in watchPreferenceChangeInterfaces) {
                                watchPreferenceChangeInterface.boolPreferenceChanged(boolPreference)
                            }
                        }
                    }
                    true
                }
                is Int -> {
                    withContext(Dispatchers.IO) {
                        val intPreference = IntPreference(id, key, newValue)
                        database.intPreferenceDao().update(intPreference)
                        withContext(Dispatchers.Main) {
                            for (watchPreferenceChangeInterface in watchPreferenceChangeInterfaces) {
                                watchPreferenceChangeInterface.intPreferenceChanged(intPreference)
                            }
                        }
                    }
                    true
                }
                else -> false
            }
        }
        return false
    }

    suspend fun updateWatchNickname(watchId: String, nickname: String): Boolean {
        if (database.isOpen) {
            withContext(Dispatchers.IO) {
                database.watchDao().setWatchName(watchId, nickname)
            }
            return true
        }
        return false
    }

    suspend fun addWatch(watch: Watch): Boolean {
        return withContext(Dispatchers.IO) {
            if (database.isOpen) {
                database.watchDao().add(watch)
                return@withContext true
            }
            return@withContext false
        }
    }

    suspend fun forgetWatch(watchId: String?): Boolean {
        return withContext(Dispatchers.IO) {
            if (!watchId.isNullOrEmpty() && database.isOpen) {
                val success = clearPreferencesForWatch(watchId)
                database.watchDao().remove(watchId)
                return@withContext success
            }
            return@withContext false
        }
    }

    suspend fun clearPreferencesForWatch(watchId: String?): Boolean {
        return withContext(Dispatchers.IO) {
            if (!watchId.isNullOrEmpty() && database.isOpen) {
                database.intPreferenceDao().deleteAllForWatch(watchId)
                database.boolPreferenceDao().deleteAllForWatch(watchId)
                forceSyncPreferences(watchId)
                if (watchId == connectedWatch?.id) {
                    updateLocalPreferences()
                }
                return@withContext true
            }
            return@withContext false
        }
    }

    suspend fun getBoolPrefsForRegisteredWatches(key: String): Array<BoolPreference>? {
        return withContext(Dispatchers.IO) {
            if (database.isOpen) {
                return@withContext database.boolPreferenceDao().getAllForKey(key)
            }
            return@withContext null
        }
    }

    private fun deleteLocalPreferences() {
        sharedPreferences.edit {
            remove(PreferenceKey.PHONE_LOCKING_ENABLED_KEY)
            remove(PreferenceKey.BATTERY_SYNC_ENABLED_KEY)
            remove(PreferenceKey.BATTERY_PHONE_CHARGE_NOTI_KEY)
            remove(PreferenceKey.BATTERY_WATCH_CHARGE_NOTI_KEY)
            remove(PreferenceKey.DND_SYNC_TO_PHONE_KEY)
            remove(PreferenceKey.DND_SYNC_TO_WATCH_KEY)
            remove(PreferenceKey.DND_SYNC_WITH_THEATER_KEY)
            remove(PreferenceKey.BATTERY_CHARGE_THRESHOLD_KEY)
        }
    }

    private suspend fun updateLocalPreferences() {
        val watch = getConnectedWatch() ?: return
        withContext(Dispatchers.Default) {
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
    }

    suspend fun getWatchByBatterySyncJobId(batterySyncJobId: Int): Watch? {
        return withContext(Dispatchers.IO) {
            if (database.isOpen and batterySyncJobId != 0) {
                return@withContext database.watchDao().findByBatterySyncJobId(batterySyncJobId)
            }
            return@withContext null
        }
    }

    private fun ensureWatchRegistered(node: Node) {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                if (database.watchDao().findById(node.id) == null) {
                    val newWatch = Watch(node)
                    database.watchDao().add(newWatch)
                    withContext(Dispatchers.Main) {
                        for (connectionInterface in watchConnectionInterfaces) {
                            connectionInterface.onWatchAdded(newWatch)
                        }
                    }
                }
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
