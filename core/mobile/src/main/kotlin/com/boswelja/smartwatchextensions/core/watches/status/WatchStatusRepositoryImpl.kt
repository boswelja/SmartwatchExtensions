package com.boswelja.smartwatchextensions.core.watches.status

import com.google.android.gms.wearable.NodeClient
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.tasks.await

internal class WatchStatusRepositoryImpl(
    private val nodeClient: NodeClient
) : WatchStatusRepository {
    override fun getStatusFor(watchId: String): Flow<ConnectionMode> = flow {
        while (currentCoroutineContext().isActive) {
            val connectedNodes = nodeClient.connectedNodes.await()
            val node = connectedNodes.firstOrNull { it.id == watchId }
            val connectionMode = node?.let {
                // If NodeClient considers the node to be nearby, assume a bluetooth connection
                if (it.isNearby) ConnectionMode.Bluetooth else ConnectionMode.Internet
            } ?: ConnectionMode.Disconnected
            emit(connectionMode)
            delay(2000)
        }
    }
}
