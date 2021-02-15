package com.boswelja.devicemanager.watchmanager.connection.wearos

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.boswelja.devicemanager.common.Event
import com.boswelja.devicemanager.common.References.CAPABILITY_WATCH_APP
import com.boswelja.devicemanager.common.connection.Capability
import com.boswelja.devicemanager.common.connection.Messages.CLEAR_PREFERENCES
import com.boswelja.devicemanager.common.connection.Messages.RESET_APP
import com.boswelja.devicemanager.common.preference.SyncPreferences
import com.boswelja.devicemanager.watchmanager.connection.WatchConnectionInterface
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.experimental.or
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

    private val _watchCapabilities = MutableLiveData<Map<String, Short>>(emptyMap())
    private val _availableWatches = MediatorLiveData<List<Watch>>()

    override val dataChanged: Event = Event()

    override val availableWatches: LiveData<List<Watch>>
        get() = _availableWatches

    override val watchCapabilities: LiveData<Map<String, Short>>
        get() = _watchCapabilities

    override val platformIdentifier: String = PLATFORM

    init {
        Timber.i("Creating WearOSConnectionInterface")
        // Set up _availableWatches
        _availableWatches.addSource(dataChanged) {
            if (it) {
                val watches = nodesWithApp.intersect(connectedNodes).map { node ->
                    Watch(node.id, node.displayName, PLATFORM).apply {
                        getWatchStatus(this, false)
                    }
                }
                Timber.d("Data changed, updating ${watches.count()} availableWatch status")
                _availableWatches.postValue(watches)
            }
        }
        _availableWatches.addSource(_watchCapabilities) {
            val watches = _availableWatches.value ?: emptyList()
            watches.map { watch ->
                val capabilities = it[watch.id] ?: watch.capabilities
                watch.capabilities = capabilities
                watch
            }
            _availableWatches.postValue(watches)
        }
        refreshData()
    }

    override fun getWatchStatus(watch: Watch, isRegistered: Boolean): Watch.Status {
        Timber.d("getWatchStatus($watch, $isRegistered) called")
        val hasWatchApp = nodesWithApp.any { it.id == watch.id }
        val isConnected = connectedNodes.any { it.id == watch.id }
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

    override fun updatePreferenceOnWatch(watch: Watch, key: String, value: Any) {
        val syncedPrefUpdateReq = PutDataMapRequest.create("/preference-change_${watch.id}")
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

    override fun resetWatchApp(watch: Watch) {
        sendMessage(watch.id, RESET_APP)
    }

    override fun resetWatchPreferences(watch: Watch) {
        sendMessage(watch.id, CLEAR_PREFERENCES)
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

    private fun refreshConnectedNodes() {
        try {
            val result = Tasks.await(nodeClient.connectedNodes)
            Timber.d("Found ${result.count()} connected nodes")
            connectedNodes = result
        } catch(e: Exception) {
            Timber.e(e)
        }
    }

    private fun refreshNodesWithApp() {
        try {
            val result = Tasks.await(
                capabilityClient.getCapability(CAPABILITY_WATCH_APP, CapabilityClient.FILTER_ALL)
            )
            Timber.d("Found ${result.nodes.count()} nodes with app")
            nodesWithApp = result.nodes.toList()
        } catch(e: Exception) {
            Timber.e(e)
        }
    }

    private fun refreshCapabilities() {
        try {
            val capabilityMap = HashMap<String, Short>()
            Capability.values().forEach { capability ->
                val result = Tasks.await(
                    capabilityClient.getCapability(capability.name, CapabilityClient.FILTER_ALL)
                )
                result.nodes.forEach { node ->
                    val capabilities = capabilityMap[node.id] ?: 0
                    capabilityMap[node.id] = capabilities or capability.id
                }
            }
            _watchCapabilities.postValue(capabilityMap)
        } catch(e: Exception) {
            Timber.e(e)
        }
    }

    companion object {
        const val PLATFORM = "WEAR_OS"
    }
}
