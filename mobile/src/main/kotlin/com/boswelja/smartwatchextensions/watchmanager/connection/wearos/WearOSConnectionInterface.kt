package com.boswelja.smartwatchextensions.watchmanager.connection.wearos

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.boswelja.smartwatchextensions.common.Event
import com.boswelja.smartwatchextensions.common.connection.Capability
import com.boswelja.smartwatchextensions.common.connection.References.CAPABILITY_WATCH_APP
import com.boswelja.smartwatchextensions.common.preference.SyncPreferences
import com.boswelja.smartwatchextensions.watchmanager.connection.WatchConnectionInterface
import com.boswelja.smartwatchextensions.watchmanager.item.Watch
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlin.experimental.or
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class WearOSConnectionInterface(
    private val capabilityClient: CapabilityClient,
    private val nodeClient: NodeClient,
    private val messageClient: MessageClient,
    private val dataClient: DataClient,
    private val coroutineScope: CoroutineScope
) : WatchConnectionInterface {

    constructor(context: Context) : this(
        Wearable.getCapabilityClient(context),
        Wearable.getNodeClient(context),
        Wearable.getMessageClient(context),
        Wearable.getDataClient(context),
        CoroutineScope(Dispatchers.IO)
    )

    @VisibleForTesting internal var nodesWithApp: List<Node> = emptyList()
    @VisibleForTesting internal var connectedNodes: List<Node> = emptyList()

    private var _watchCapabilities: Map<String, Short> = emptyMap()
    private val _availableWatches = MediatorLiveData<List<Watch>>()

    override val dataChanged: Event = Event()

    override val availableWatches: LiveData<List<Watch>>
        get() = _availableWatches

    override val watchCapabilities: Map<String, Short>
        get() = _watchCapabilities

    override val platformIdentifier: String = PLATFORM

    init {
        Timber.i("Creating WearOSConnectionInterface")
        // Set up _availableWatches
        _availableWatches.addSource(dataChanged) {
            if (it) {
                val watches = nodesWithApp.intersect(connectedNodes).map { node ->
                    Watch(node.id, node.displayName, PLATFORM).apply {
                        getWatchStatus(id, false)
                        capabilities = _watchCapabilities[id] ?: 0
                    }
                }
                Timber.d("Data changed, updating ${watches.count()} availableWatch status")
                _availableWatches.postValue(watches)
            }
        }
        refreshData()
    }

    override fun getWatchStatus(watchId: String, isRegistered: Boolean): Watch.Status {
        Timber.d("getWatchStatus($watchId, $isRegistered) called")
        val hasWatchApp = nodesWithApp.any { it.id == watchId }
        val isConnected = connectedNodes.any { it.id == watchId }
        return when {
            hasWatchApp && isConnected && isRegistered -> Watch.Status.CONNECTED
            hasWatchApp && isConnected && !isRegistered -> Watch.Status.NOT_REGISTERED
            hasWatchApp && !isConnected && isRegistered -> Watch.Status.DISCONNECTED
            !hasWatchApp && isConnected -> Watch.Status.MISSING_APP
            else -> Watch.Status.ERROR
        }
    }

    override fun sendMessage(watchId: String, path: String, data: ByteArray?) {
        messageClient.sendMessage(watchId, path, data)
    }

    override fun updatePreferenceOnWatch(watchId: String, key: String, value: Any) {
        val syncedPrefUpdateReq = PutDataMapRequest.create("/preference-change_$watchId")
        when (key) {
            in SyncPreferences.BOOL_PREFS -> {
                if (value is Boolean) syncedPrefUpdateReq.dataMap.putBoolean(key, value)
                else Timber.w("Invalid value ($value) for preference $key")
            }
            in SyncPreferences.INT_PREFS -> {
                if (value is Int) syncedPrefUpdateReq.dataMap.putInt(key, value)
                else Timber.w("Invalid value ($value) for preference $key")
            }
        }
        if (!syncedPrefUpdateReq.dataMap.isEmpty) {
            Timber.i("Sending updated preference")
            syncedPrefUpdateReq.setUrgent()
            dataClient.putDataItem(syncedPrefUpdateReq.asPutDataRequest())
        } else {
            Timber.w("No preference to update")
        }
    }

    override fun refreshData() {
        Timber.d("refreshData() called")
        coroutineScope.launch {
            refreshConnectedNodes()
            refreshNodesWithApp()
            refreshCapabilities()
            dataChanged.fire()
        }
    }

    @VisibleForTesting internal suspend fun refreshConnectedNodes() {
        try {
            val result = nodeClient.connectedNodes.await()
            Timber.d("Found ${result.count()} connected nodes")
            connectedNodes = result
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    @VisibleForTesting internal suspend fun refreshNodesWithApp() {
        try {
            val result = capabilityClient.getCapability(
                CAPABILITY_WATCH_APP, CapabilityClient.FILTER_ALL
            ).await()
            Timber.d("Found ${result.nodes.count()} nodes with app")
            nodesWithApp = result.nodes.toList()
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    @VisibleForTesting internal suspend fun refreshCapabilities() {
        try {
            val capabilityMap = HashMap<String, Short>()
            Capability.values().forEach { capability ->
                val result = capabilityClient.getCapability(
                    capability.name, CapabilityClient.FILTER_ALL
                ).await()
                result.nodes.forEach { node ->
                    val capabilities = capabilityMap[node.id] ?: 0
                    capabilityMap[node.id] = capabilities or capability.id
                }
                Timber.d("Found ${result.nodes.count()} nodes with capability $capability")
            }
            _watchCapabilities = capabilityMap
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    companion object {
        const val PLATFORM = "WEAR_OS"
    }
}
