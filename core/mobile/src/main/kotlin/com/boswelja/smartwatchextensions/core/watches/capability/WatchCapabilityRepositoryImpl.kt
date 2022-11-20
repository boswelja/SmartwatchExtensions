package com.boswelja.smartwatchextensions.core.watches.capability

import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityClient.OnCapabilityChangedListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class WatchCapabilityRepositoryImpl(
    private val capabilityClient: CapabilityClient
) : WatchCapabilityRepository {
    override fun hasCapability(watchId: String, capability: String): Flow<Boolean> = callbackFlow {
        val callback = OnCapabilityChangedListener { capabilityInfo ->
            trySend(capabilityInfo.nodes.any { it.id == watchId })
        }

        send(
            capabilityClient.getCapability(capability, CapabilityClient.FILTER_ALL).await().nodes
                .any { it.id == watchId }
        )

        capabilityClient.addListener(callback, capability).await()

        awaitClose {
            capabilityClient.removeListener(callback)
        }
    }
}
