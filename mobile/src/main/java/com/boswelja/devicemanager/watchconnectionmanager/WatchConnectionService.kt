package com.boswelja.devicemanager.watchconnectionmanager

import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.os.Binder
import android.os.IBinder
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.room.Room
import com.boswelja.devicemanager.common.References
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.Wearable

class WatchConnectionService :
        Service(),
        CapabilityClient.OnCapabilityChangedListener {

    private val binder = WatchConnectionServiceBinder()

    private val watchConnectionInterfaces: ArrayList<WatchConnectionInterface> = ArrayList()

    private lateinit var database: WatchDatabase
    private lateinit var capabilityClient: CapabilityClient
    private lateinit var sharedPreferences: SharedPreferences
    private var connectedWatch: Watch? = null

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

        connectedWatch = database.watchDao().findById(sharedPreferences.getString(LAST_CONNECTED_NODE_ID_KEY, "0")!!)

        capabilityClient = Wearable.getCapabilityClient(this)
        capabilityClient.addListener(this, References.CAPABILITY_WATCH_APP)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()

        if (database.isOpen) database.close()

        sharedPreferences.edit {
            putString(LAST_CONNECTED_NODE_ID_KEY, connectedWatch?.id)
            apply()
        }
    }

    public fun getAllWatches(): List<Watch>? {
        if (database.isOpen) {
            return database.watchDao().getAll()
        }
        return null
    }

    public fun getConnectedWatch(): Watch? = connectedWatch

    companion object {
        private const val LAST_CONNECTED_NODE_ID_KEY = "last_connected_id"
    }

    inner class WatchConnectionServiceBinder: Binder() {
        public fun getService(): WatchConnectionService =
                this@WatchConnectionService
    }
}