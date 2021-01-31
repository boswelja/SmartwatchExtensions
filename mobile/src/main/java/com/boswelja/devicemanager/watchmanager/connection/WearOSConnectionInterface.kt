package com.boswelja.devicemanager.watchmanager.connection

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.boswelja.devicemanager.common.Event
import com.boswelja.devicemanager.common.References.CAPABILITY_WATCH_APP
import com.boswelja.devicemanager.common.preference.SyncPreferences
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import timber.log.Timber

class WearOSConnectionInterface(
    private val capabilityClient: CapabilityClient,
    private val nodeClient: NodeClient,
    private val messageClient: MessageClient,
    private val dataClient: DataClient
) : WatchConnectionInterface {

    constructor(context: Context) : this(
        Wearable.getCapabilityClient(context),
        Wearable.getNodeClient(context),
        Wearable.getMessageClient(context),
        Wearable.getDataClient(context)
    )

    @VisibleForTesting internal var nodesWithApp: Set<Node> = emptySet()
    @VisibleForTesting internal var connectedNodes: MutableLiveData<List<Node>> =
        MutableLiveData(emptyList())

    private val capableWatchesListener = CapabilityClient.OnCapabilityChangedListener {
        Timber.d("${it.name} capability changed")
        nodesWithApp = it.nodes
        dataChanged.fire()
    }

    private val _availableWatches = MediatorLiveData<List<Watch>>()

    override val dataChanged: Event = Event()

    override val availableWatches: LiveData<List<Watch>>
        get() = _availableWatches

    init {
        // Set up connectedNodes and nodesWithApp updates
        capabilityClient.addListener(capableWatchesListener, CAPABILITY_WATCH_APP)
        capabilityClient.getCapability(CAPABILITY_WATCH_APP, CapabilityClient.FILTER_ALL)
            .addOnSuccessListener {
                nodesWithApp = it.nodes
                dataChanged.fire()
            }
            .addOnFailureListener { Timber.w("Failed to get capable nodes") }
        refreshConnectedNodes()

        // Set up _availableWatches
        _availableWatches.addSource(connectedNodes) { connectedNodes ->
            val watches = connectedNodes.intersect(nodesWithApp).map {
                Watch(it.id, it.displayName, PLATFORM).apply {
                    getWatchStatus(this, false)
                }
            }
            _availableWatches.postValue(watches)
        }
        _availableWatches.addSource(dataChanged) {
            if (it) {
                val watches = _availableWatches.value?.map { watch ->
                    watch.status = getWatchStatus(watch, false)
                    watch
                }
                _availableWatches.postValue(watches ?: emptyList())
            }
        }
    }

    override fun getPlatformIdentifier(): String = PLATFORM

    override fun getAvailableWatches(): List<Watch> = availableWatches.value ?: emptyList()

    override fun getWatchStatus(watch: Watch, isRegistered: Boolean): Watch.Status {
        Timber.d("getWatchStatus($watch, $isRegistered) called")
        val hasWatchApp = nodesWithApp.any { it.id == watch.id }
        val isConnected = connectedNodes.value!!.any { it.id == watch.id }
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

    internal fun refreshConnectedNodes(): Task<List<Node>> {
        return nodeClient.connectedNodes
            .addOnSuccessListener {
                connectedNodes.postValue(it)
                dataChanged.fire()
            }
            .addOnFailureListener {
                Timber.e(it)
            }
    }

    fun dispose() {
        capabilityClient.removeListener(capableWatchesListener, CAPABILITY_WATCH_APP)
    }

    companion object {
        const val PLATFORM = "WEAR_OS"
    }
}
