package com.boswelja.smartwatchextensions.phoneconnectionmanager

import android.content.Context
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

/**
 * A helper class for managing the connection to a phone.
 * @param capabilityClient [CapabilityClient].
 * @param refreshInterval The time in milliseconds to wait between refreshes.
 */
class ConnectionHelper(
    private val capabilityClient: CapabilityClient,
    private val refreshInterval: Long
) {

    /**
     * A helper class for managing the connection to a phone.
     * @param context [Context].
     * @param refreshInterval The time in milliseconds to wait between refreshes.
     */
    constructor(context: Context, refreshInterval: Long = 5000) : this(
        Wearable.getCapabilityClient(context),
        refreshInterval
    )

    fun phoneStatus(): Flow<Status> = flow {
        while (true) {
            val capabilityInfo = capabilityClient
                .getCapability(CAPABILITY_PHONE_APP, CapabilityClient.FILTER_REACHABLE)
                .await()

            // We can assume the phone is the only one on the list
            val phoneNode = capabilityInfo.nodes.firstOrNull()

            // Determine the phone status
            val status = phoneNode?.let {
                if (it.isNearby) Status.CONNECTED_NEARBY
                else Status.CONNECTED
            } ?: Status.DISCONNECTED
            emit(status)

            // Wait for a specified interval before repeating
            delay(refreshInterval)
        }
    }

    companion object {
        const val CAPABILITY_PHONE_APP = "extensions_phone_app"
    }
}

enum class Status {
    DISCONNECTED,
    CONNECTED,
    CONNECTED_NEARBY,
}
