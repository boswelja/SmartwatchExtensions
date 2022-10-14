package com.boswelja.smartwatchextensions.wearableinterface.playservices

import android.content.Context
import com.boswelja.smartwatchextensions.wearableinterface.CapabilityManager
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

internal class CapabilityManagerImpl(context: Context) : CapabilityManager {

    private val capabilityClient = Wearable.getCapabilityClient(context)

    override fun addLocalCapability(capability: String) {
        capabilityClient.addLocalCapability(capability)
    }

    override fun removeLocalCapability(capability: String) {
        capabilityClient.removeLocalCapability(capability)
    }

    override suspend fun getCapabilitiesFor(watchId: String): Set<String> {
        val capabilities = capabilityClient.getAllCapabilities(CapabilityClient.FILTER_ALL).await()
        return capabilities.filterValues { it.nodes.any { it.id == watchId } }.keys
    }
}
