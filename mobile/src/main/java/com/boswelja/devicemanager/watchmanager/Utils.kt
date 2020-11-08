package com.boswelja.devicemanager.watchmanager

import com.boswelja.devicemanager.watchmanager.database.WatchDatabase
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.google.android.gms.wearable.Node
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal object Utils {

    /**
     * Gets the status of a specified [Watch].
     * @param watchId The [Watch.id] to find a [WatchStatus] for.
     * @param capableNodes The [Set] of capable [Node] objects to check against. Default is null.
     * @param connectedNodes The [List] of connected [Node] objects to check against. Default is
     * null.
     * @return A [WatchStatus] for the [Watch].
     */
    internal suspend fun getWatchStatus(
        watchId: String,
        database: WatchDatabase,
        capableNodes: Set<Node>? = null,
        connectedNodes: List<Node>? = null
    ): WatchStatus {
        return withContext(Dispatchers.IO) {
            val isCapable = capableNodes?.any { it.id == watchId } ?: false
            val isConnected = connectedNodes?.any { it.id == watchId } ?: false
            val isRegistered = database.watchDao().get(watchId) != null
            return@withContext when {
                isCapable && isConnected && isRegistered -> WatchStatus.CONNECTED
                isCapable && !isConnected && isRegistered -> WatchStatus.DISCONNECTED
                isCapable && !isRegistered -> WatchStatus.NOT_REGISTERED
                !isCapable && !isRegistered -> WatchStatus.MISSING_APP
                else -> WatchStatus.ERROR
            }
        }
    }
}
