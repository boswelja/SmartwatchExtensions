package com.boswelja.smartwatchextensions.core.watches.capability

import kotlinx.coroutines.flow.Flow

interface WatchCapabilityRepository {

    /**
     * Flow whether a watch with the given ID has announced the given capability.
     */
    fun hasCapability(watchId: String, capability: String): Flow<Boolean>
}
