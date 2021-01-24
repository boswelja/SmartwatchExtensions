package com.boswelja.devicemanager.watchmanager

import com.boswelja.devicemanager.common.References
import com.boswelja.devicemanager.watchmanager.database.WatchDatabase
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.NodeClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

internal object Utils {

    /**
     * Gets the status of a specified [Watch].
     * @param watchId The [Watch.id] to find a [Watch.Status] for.
     * @param capableNodes The [Set] of capable [Node] objects to check against. Default is null.
     * @param connectedNodes The [List] of connected [Node] objects to check against. Default is
     * null.
     * @return A [Watch.Status] for the [Watch].
     */
    internal suspend fun getWatchStatus(
        watchId: String,
        database: WatchDatabase,
        capableNodes: Set<Node>? = null,
        connectedNodes: List<Node>? = null
    ): Watch.Status {
        return withContext(Dispatchers.IO) {
            val isCapable = capableNodes?.any { it.id == watchId } ?: false
            val isConnected = connectedNodes?.any { it.id == watchId } ?: false
            val isRegistered = database.watchDao().get(watchId) != null
            return@withContext when {
                isCapable && isConnected && isRegistered -> Watch.Status.CONNECTED
                !isConnected && isRegistered -> Watch.Status.DISCONNECTED
                isCapable && !isRegistered -> Watch.Status.NOT_REGISTERED
                !isCapable && !isRegistered -> Watch.Status.MISSING_APP
                else -> Watch.Status.ERROR
            }
        }
    }

    /**
     * Get a [List] of connected [Node], regardless of capability.
     * @return The [List] of connected [Node], or null if the task failed.
     */
    internal suspend fun getConnectedNodes(nodeClient: NodeClient): List<Node>? {
        Timber.d("getConnectedNodes() called")
        return try {
            withContext(Dispatchers.IO) { Tasks.await(nodeClient.connectedNodes) }
        } catch (e: Exception) {
            Timber.w(e)
            null
        }
    }

    /**
     * Gets the [Set] of capable [Node]. Each [Node] declares [References.CAPABILITY_WATCH_APP], and
     * is reachable at the time of checking.
     * @return The [Set] of capable [Node], or null if the task failed.
     */
    internal suspend fun getCapableNodes(capabilityClient: CapabilityClient): Set<Node>? {
        Timber.d("getCapableNodes() called")
        var capableNodes: Set<Node>? = null
        try {
            withContext(Dispatchers.IO) {
                capableNodes =
                    Tasks.await(
                        capabilityClient.getCapability(
                            References.CAPABILITY_WATCH_APP, CapabilityClient.FILTER_REACHABLE
                        )
                    ).nodes
            }
        } catch (e: Exception) {
            Timber.w(e)
        }
        return capableNodes
    }
}
