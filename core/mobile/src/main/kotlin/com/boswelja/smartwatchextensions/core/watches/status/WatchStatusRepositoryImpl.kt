package com.boswelja.smartwatchextensions.core.watches.status

import com.boswelja.watchconnection.core.discovery.DiscoveryClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest

internal class WatchStatusRepositoryImpl(
    private val discoveryClient: DiscoveryClient
) : WatchStatusRepository {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getStatusFor(watchId: String): Flow<ConnectionMode> {
        return discoveryClient.connectionModeFor(watchId)
            .mapLatest {
                when (it) {
                    com.boswelja.watchconnection.common.discovery.ConnectionMode.Disconnected ->
                        ConnectionMode.Disconnected
                    com.boswelja.watchconnection.common.discovery.ConnectionMode.Internet ->
                        ConnectionMode.Internet
                    com.boswelja.watchconnection.common.discovery.ConnectionMode.Bluetooth ->
                        ConnectionMode.Bluetooth
                }
            }
    }
}
