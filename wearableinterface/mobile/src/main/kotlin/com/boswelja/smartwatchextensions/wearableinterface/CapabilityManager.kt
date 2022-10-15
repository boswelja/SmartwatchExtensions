package com.boswelja.smartwatchextensions.wearableinterface

interface CapabilityManager {
    fun addLocalCapability(capability: String)

    fun removeLocalCapability(capability: String)

    suspend fun getCapabilitiesFor(watchId: String): Set<String>
}
