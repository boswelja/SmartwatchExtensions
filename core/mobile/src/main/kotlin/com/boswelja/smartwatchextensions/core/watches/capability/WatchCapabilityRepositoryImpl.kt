package com.boswelja.smartwatchextensions.core.watches.capability

import com.google.android.gms.wearable.CapabilityClient
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class WatchCapabilityRepositoryImpl(
    private val capabilityClient: CapabilityClient
) : WatchCapabilityRepository {
    override fun hasCapability(targetId: String, capability: String): Flow<Boolean> = callbackFlow {
        val listener = CapabilityClient.OnCapabilityChangedListener { capabilityInfo ->
            trySend(capabilityInfo.nodes.any { it.id == targetId })
        }

        // Get the capability info immediately
        val hasCapability = capabilityClient
            .getCapability(capability, CapabilityClient.FILTER_ALL)
            .await()
            .nodes
            .any { it.id == targetId }
        send(hasCapability)

        capabilityClient.addListener(listener, capability)

        awaitClose {
            capabilityClient.removeListener(listener)
        }
    }
}
