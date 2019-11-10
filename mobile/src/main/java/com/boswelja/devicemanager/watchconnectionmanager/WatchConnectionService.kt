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
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable

class WatchConnectionService :
        Service(),
        CapabilityClient.OnCapabilityChangedListener {

    private val binder = WatchConnectionServiceBinder()

    private val watchConnectionInterfaces: ArrayList<WatchConnectionInterface> = ArrayList()

    private lateinit var database: WatchDatabase
    private lateinit var capabilityClient: CapabilityClient
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var dataClient: DataClient

    private var preferenceChangePath = ""

    private var connectedWatchId: String = "0"

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        for (node in capabilityInfo.nodes) {
            if (database.watchDao().findById(node.id) == null) {
                database.watchDao().add(Watch(node.id, node.displayName, hasApp = true))
                for (connectionInterface in watchConnectionInterfaces) {
                    connectionInterface.onWatchAdded()
                }
            } else {
                database.watchDao().setHasApp(node.id, true)
                for (connectionInterface in watchConnectionInterfaces) {
                    connectionInterface.onWatchInfoUpdated()
                }
            }
        }
    }

    override fun onBind(p0: Intent?): IBinder? = binder

    override fun onCreate() {
        super.onCreate()

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        database = Room.databaseBuilder(
                applicationContext,
                WatchDatabase::class.java,
                "watch-db"
        ).build()

        connectedWatchId = sharedPreferences.getString(LAST_CONNECTED_NODE_ID_KEY, "0") ?: "0"

        capabilityClient = Wearable.getCapabilityClient(this)
        capabilityClient.addListener(this, References.CAPABILITY_WATCH_APP)

        dataClient = Wearable.getDataClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()

        if (database.isOpen) database.close()

        sharedPreferences.edit {
            putString(LAST_CONNECTED_NODE_ID_KEY, connectedWatchId)
        }
    }

    public fun getAllWatches(): List<Watch>? {
        if (database.isOpen) {
            return database.watchDao().getAll()
        }
        return null
    }

    public fun getConnectedWatch(): Watch? = database.watchDao().findById(connectedWatchId)

    public fun setConnectedWatchById(id: String): Boolean {
        for (connectionInterface in watchConnectionInterfaces) {
            connectionInterface.onConnectedWatchChanging()
        }

        database.watchDao().findById(id) ?: return false
        connectedWatchId = id
        preferenceChangePath = "/preference-change_${connectedWatchId}"

        updateLocalPreferences()

        for (connectionInterface in watchConnectionInterfaces) {
            connectionInterface.onConnectedWatchChanged()
        }
        return true
    }

    public fun forceSyncPreferences(): Task<DataItem>? {
        if (connectedWatchId != "0") {
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

    public fun updatePreference(key: String): Task<DataItem>? {
        if (connectedWatchId != "0") {
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

    private fun updateLocalPreferences() {
        val watch = database.watchDao().findById(connectedWatchId) ?: return
        sharedPreferences.edit {
            watch.boolPrefs.forEach { (key, value) ->
                putBoolean(key, value)
            }
            watch.intPrefs.forEach { (key, value) ->
                putInt(key, value)
            }
        }
    }

    companion object {
        private const val LAST_CONNECTED_NODE_ID_KEY = "last_connected_id"

        public fun bind(context: Context, serviceConnection: Connection) {
            context.bindService(Intent(context, WatchConnectionService::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    inner class WatchConnectionServiceBinder: Binder() {
        public fun getService(): WatchConnectionService =
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