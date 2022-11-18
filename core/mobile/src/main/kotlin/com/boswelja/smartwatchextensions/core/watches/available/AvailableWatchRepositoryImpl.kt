package com.boswelja.smartwatchextensions.core.watches.available

import com.google.android.gms.wearable.CapabilityClient
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * An implementation of [AvailableWatchRepository] backed by [CapabilityClient].
 */
class AvailableWatchRepositoryImpl(
    private val capabilityClient: CapabilityClient
) : AvailableWatchRepository {
    override fun getAvailableWatches(): Flow<List<AvailableWatch>> = callbackFlow {
        val callback = CapabilityClient.OnCapabilityChangedListener { capabilityInfo ->
            trySend(
                capabilityInfo.nodes.map {
                    AvailableWatch(
                        it.id,
                        it.displayName
                    )
                }
            )
        }
        val capabilities = capabilityClient.getCapability(WATCH_CAPABILITY, CapabilityClient.FILTER_REACHABLE).await()
        send(
            capabilities.nodes.map {
                AvailableWatch(
                    it.id,
                    it.displayName
                )
            }
        )

        capabilityClient.addListener(callback, WATCH_CAPABILITY)

        awaitClose {
            capabilityClient.removeListener(callback)
        }
    }

    companion object {
        private const val WATCH_CAPABILITY = "extensions_wear_app"
    }
}
