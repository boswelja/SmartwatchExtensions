package com.boswelja.smartwatchextensions.core.watches.status

import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.NodeClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.tasks.await

internal class WatchStatusRepositoryImpl(
    private val discoveryClient: NodeClient
) : WatchStatusRepository {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getStatusFor(watchId: String): Flow<ConnectionMode> {
        return flow<List<Node>> { discoveryClient.connectedNodes.await() }
            .mapLatest { connectedNodes ->
                connectedNodes.firstOrNull { it.id == watchId }
            }
            .mapLatest { node ->
                when {
                    node == null -> ConnectionMode.Disconnected
                    node.isNearby -> ConnectionMode.Bluetooth
                    else -> ConnectionMode.Internet
                }
            }
    }
}
