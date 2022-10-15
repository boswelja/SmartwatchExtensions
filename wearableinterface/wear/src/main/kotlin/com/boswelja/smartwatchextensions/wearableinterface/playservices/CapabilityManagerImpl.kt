package com.boswelja.smartwatchextensions.wearableinterface.playservices

import android.content.Context
import com.boswelja.smartwatchextensions.wearableinterface.CapabilityManager
import com.google.android.gms.wearable.Wearable

internal class CapabilityManagerImpl(context: Context) : CapabilityManager {

    private val capabilityClient = Wearable.getCapabilityClient(context)

    override fun addLocalCapability(capability: String) {
        capabilityClient.addLocalCapability(capability)
    }

    override fun removeLocalCapability(capability: String) {
        capabilityClient.removeLocalCapability(capability)
    }
}
