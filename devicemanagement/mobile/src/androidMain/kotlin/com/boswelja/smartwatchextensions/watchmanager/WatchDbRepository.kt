package com.boswelja.smartwatchextensions.watchmanager

import com.boswelja.smartwatchextensions.devicemanagement.Capability
import com.boswelja.smartwatchextensions.watchmanager.database.RegisteredWatch
import com.boswelja.smartwatchextensions.watchmanager.database.RegisteredWatchDatabase
import com.boswelja.watchconnection.common.Watch
import com.boswelja.watchconnection.common.discovery.ConnectionMode
import com.boswelja.watchconnection.core.discovery.DiscoveryClient
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOne
import kotlinx.coroutines.flow.Flow

class WatchDbRepository(
    private val discoveryClient: DiscoveryClient,
    private val database: RegisteredWatchDatabase
) : WatchRepository {

    override val registeredWatches: Flow<List<Watch>>
        get() = database.registeredWatchQueries
            .getAll { uid, name, _ ->
                Watch(uid, name)
            }
            .asFlow()
            .mapToList()

    override val availableWatches: Flow<List<Watch>>
        get() = discoveryClient.allWatches()

    override suspend fun registerWatch(watch: Watch) {
        database.registeredWatchQueries.insert(
            RegisteredWatch(
                watch.uid,
                watch.name,
                watch.platform
            )
        )
    }

    override suspend fun deregisterWatch(watch: Watch) {
        database.registeredWatchQueries.delete(watch.uid)
    }

    override suspend fun renameWatch(watch: Watch, newName: String) {
        database.registeredWatchQueries.rename(watch.uid, newName)
    }

    override suspend fun getCapabilitiesFor(watch: Watch): List<Capability> =
        discoveryClient.getCapabilitiesFor(watch).map { capability ->
            Capability.valueOf(capability)
        }

    override fun getStatusFor(watch: Watch): Flow<ConnectionMode> =
        discoveryClient.connectionModeFor(watch)

    override fun getWatchById(id: String): Flow<Watch?> = database.registeredWatchQueries
        .get(id) { uid, name, _ ->
            Watch(uid, name)
        }
        .asFlow()
        .mapToOne()

    override fun watchHasCapability(watch: Watch, capability: Capability): Flow<Boolean> =
        discoveryClient.hasCapability(watch, capability.name)
}
