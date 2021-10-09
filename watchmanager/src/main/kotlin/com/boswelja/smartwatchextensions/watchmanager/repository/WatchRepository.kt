package com.boswelja.smartwatchextensions.watchmanager.repository

import android.content.Context
import com.boswelja.smartwatchextensions.common.connection.Capability
import com.boswelja.smartwatchextensions.watchmanager.repository.DbWatch.Companion.toDbWatch
import com.boswelja.watchconnection.common.Watch
import com.boswelja.watchconnection.common.discovery.ConnectionMode
import com.boswelja.watchconnection.core.discovery.DiscoveryClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * A repository for managing registered watches, and discovering available watches.
 */
class WatchRepository(
    context: Context,
    private val discoveryClient: DiscoveryClient
) {

    private val watchDatabase: WatchDatabase = WatchDatabase.create(context.applicationContext)

    /**
     * Flows the list of registered watches.
     */
    val registeredWatches: Flow<List<Watch>>
        get() = watchDatabase.watchDao().getAll().map { watches -> watches.map { it.toWatch() } }

    /**
     * Flows the list of available watches. Note this includes previously registered watches
     */
    val availableWatches: Flow<List<Watch>>
        get() = discoveryClient.allWatches()

    /**
     * Register a new [Watch].
     */
    suspend fun registerWatch(watch: Watch) {
        watchDatabase.watchDao().add(watch.toDbWatch())
    }

    /**
     * Deregister an existing [Watch].
     */
    suspend fun deregisterWatch(watch: Watch) {
        watchDatabase.watchDao().remove(watch.uid)
    }

    /**
     * Rename an existing [Watch].
     */
    suspend fun renameWatch(watch: Watch, newName: String) {
        watchDatabase.watchDao().setName(watch.uid, newName)
    }

    /**
     * Get a list of all [Capability] declared by a given [Watch].
     */
    suspend fun getCapabilitiesFor(watch: Watch) =
        discoveryClient.getCapabilitiesFor(watch)?.map { capability ->
            Capability.valueOf(capability)
        }

    /**
     * Flow the [ConnectionMode] for a given [Watch].
     */
    fun getStatusFor(watch: Watch): Flow<ConnectionMode> = discoveryClient.connectionModeFor(watch)

    /**
     * Flow a given [Watch] by it's [Watch.uid].
     */
    fun getWatchById(id: String): Flow<Watch?> =
        watchDatabase.watchDao().get(id).map { it?.toWatch() }
}
