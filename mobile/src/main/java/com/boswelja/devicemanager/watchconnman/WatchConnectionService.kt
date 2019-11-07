package com.boswelja.devicemanager.watchconnman

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.room.Room
import com.boswelja.devicemanager.common.References
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.Wearable

class WatchConnectionService :
        Service(),
        CapabilityClient.OnCapabilityChangedListener {

    private val binder = WatchConnectionServiceBinder()

    private lateinit var database: WatchDatabase
    private lateinit var capabilityClient: CapabilityClient
    private lateinit var nodeClient: NodeClient

    private fun updateConnectedWatches() {
        nodeClient.connectedNodes
                .addOnSuccessListener {
                    if (it.isNotEmpty() and database.isOpen) {
                        for (node in it) {
                            if (database.watchDao().findById(node.id) == null) {
                                database.watchDao().add(Watch(node.id, node.displayName))
                            }
                        }
                    }
                }
    }

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        for (node in capabilityInfo.nodes) {
            database.watchDao().setHasApp(node.id, true)
        }
    }

    override fun onBind(p0: Intent?): IBinder? = binder

    override fun onCreate() {
        super.onCreate()

        database = Room.databaseBuilder(
                applicationContext,
                WatchDatabase::class.java,
                "watch-db"
        ).build()

        capabilityClient = Wearable.getCapabilityClient(this)
        capabilityClient.addListener(this, References.CAPABILITY_WATCH_APP)

        nodeClient = Wearable.getNodeClient(this)
        updateConnectedWatches()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (database.isOpen) database.close()
    }

    public fun getAllWatches(): List<Watch>? {
        if (database.isOpen) {
            return database.watchDao().getAll()
        }
        return null
    }

    inner class WatchConnectionServiceBinder: Binder() {
        public fun getService(): WatchConnectionService =
                this@WatchConnectionService
    }
}