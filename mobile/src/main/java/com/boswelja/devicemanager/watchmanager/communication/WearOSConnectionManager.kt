package com.boswelja.devicemanager.watchmanager.communication

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.boswelja.devicemanager.common.References.CAPABILITY_WATCH_APP
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.Wearable
import timber.log.Timber

class WearOSConnectionManager(
    private val capabilityClient: CapabilityClient,
    private val nodeClient: NodeClient,
    private val messageClient: MessageClient
) : WatchConnectionInterface {

    constructor(context: Context) : this(
        Wearable.getCapabilityClient(context),
        Wearable.getNodeClient(context),
        Wearable.getMessageClient(context)
    )

    @VisibleForTesting internal var nodesWithApp: Set<Node> = emptySet()
    @VisibleForTesting internal var connectedNodes: List<Node> = emptyList()

    private val capableWatchesListener = CapabilityClient.OnCapabilityChangedListener {
        Timber.d("${it.name} capability changed")
        nodesWithApp = it.nodes
    }

    init {
        capabilityClient.addListener(capableWatchesListener, CAPABILITY_WATCH_APP)
        refreshConnectedNodes()
    }

    override fun getAvailableWatches(): List<Watch> {
        val availableNodes = connectedNodes.intersect(nodesWithApp)
        return availableNodes.map { Watch(it.id, it.displayName, Watch.Platform.WEAR_OS) }
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

    internal fun refreshConnectedNodes(): Task<List<Node>> {
        return nodeClient.connectedNodes
            .addOnSuccessListener {
                connectedNodes = it
            }
            .addOnFailureListener {
                Timber.e(it)
            }
    }

    fun dispose() {
        capabilityClient.removeListener(capableWatchesListener, CAPABILITY_WATCH_APP)
    }
}
