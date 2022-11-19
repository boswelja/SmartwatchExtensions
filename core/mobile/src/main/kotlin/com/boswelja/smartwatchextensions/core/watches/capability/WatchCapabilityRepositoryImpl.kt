package com.boswelja.smartwatchextensions.core.watches.capability

import com.boswelja.watchconnection.common.Watch
import com.boswelja.watchconnection.core.discovery.DiscoveryClient
import kotlinx.coroutines.flow.Flow

class WatchCapabilityRepositoryImpl(
    private val discoveryClient: DiscoveryClient
) : WatchCapabilityRepository {
    override fun hasCapability(watch: Watch, capability: String): Flow<Boolean> {
        return discoveryClient.hasCapability(watch.uid, capability)
    }
}
