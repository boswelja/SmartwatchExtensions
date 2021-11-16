package com.boswelja.smartwatchextensions.devicemanagement

import com.boswelja.smartwatchextensions.devicemanagement.database.RegisteredWatch
import com.boswelja.smartwatchextensions.devicemanagement.database.RegisteredWatchDatabase
import com.boswelja.watchconnection.common.Watch
import com.boswelja.watchconnection.common.discovery.ConnectionMode
import com.boswelja.watchconnection.core.discovery.DiscoveryClient
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOneOrNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * A [WatchRepository] implementation backed by a SQLDelight database.
 */
class WatchDbRepository(
    private val discoveryClient: DiscoveryClient,
    private val database: RegisteredWatchDatabase,
    private val dispatcher: CoroutineContext
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
        withContext(dispatcher) {
            database.registeredWatchQueries.insert(
                RegisteredWatch(
                    watch.uid,
                    watch.name,
                    watch.platform
                )
            )
        }
    }

    override suspend fun deregisterWatch(watch: Watch) {
        withContext(dispatcher) {
            database.registeredWatchQueries.delete(watch.uid)
        }
    }

    override suspend fun renameWatch(watch: Watch, newName: String) {
        withContext(dispatcher) {
            database.registeredWatchQueries.rename(watch.uid, newName)
        }
    }

    override suspend fun getCapabilitiesFor(watch: Watch): List<Capability> =
        discoveryClient.getCapabilitiesFor(watch.uid).map { capability ->
            Capability.valueOf(capability)
        }

    override fun getStatusFor(watch: Watch): Flow<ConnectionMode> =
        discoveryClient.connectionModeFor(watch.uid)

    override fun getWatchById(id: String): Flow<Watch?> = database.registeredWatchQueries
        .get(id) { uid, name, _ ->
            Watch(uid, name)
        }
        .asFlow()
        .mapToOneOrNull()

    override fun watchHasCapability(watch: Watch, capability: Capability): Flow<Boolean> =
        discoveryClient.hasCapability(watch.uid, capability.name)
}
