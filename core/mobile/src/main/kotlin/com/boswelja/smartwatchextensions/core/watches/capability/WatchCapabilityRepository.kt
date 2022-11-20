package com.boswelja.smartwatchextensions.core.watches.capability

import com.boswelja.watchconnection.common.Watch
import kotlinx.coroutines.flow.Flow

interface WatchCapabilityRepository {

    /**
     * Flow whether a watch with the given ID has announced the given capability.
     */
    fun hasCapability(watch: Watch, capability: String): Flow<Boolean>
}
