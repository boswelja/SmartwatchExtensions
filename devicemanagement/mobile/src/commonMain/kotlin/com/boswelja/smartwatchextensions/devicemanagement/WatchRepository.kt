package com.boswelja.smartwatchextensions.devicemanagement

import com.boswelja.watchconnection.common.Watch
import com.boswelja.watchconnection.common.discovery.ConnectionMode
import kotlinx.coroutines.flow.Flow

/**
 * A repository for managing registered watches, and discovering available watches.
 */
interface WatchRepository {

    /**
     * Flows the list of registered watches.
     */
    val registeredWatches: Flow<List<Watch>>

    /**
     * Flows the list of available watches. Note this includes previously registered watches
     */
    val availableWatches: Flow<List<Watch>>

    /**
     * Register a new [Watch].
     */
    suspend fun registerWatch(watch: Watch)

    /**
     * Deregister an existing [Watch].
     */
    suspend fun deregisterWatch(watch: Watch)

    /**
     * Rename an existing [Watch].
     */
    suspend fun renameWatch(watch: Watch, newName: String)

    /**
     * Get a list of all [Capability] declared by a given [Watch].
     */
    suspend fun getCapabilitiesFor(watch: Watch): List<Capability>

    /**
     * Flow the [ConnectionMode] for a given [Watch].
     */
    fun getStatusFor(watch: Watch): Flow<ConnectionMode>

    /**
     * Flow a given [Watch] by it's [Watch.uid].
     */
    fun getWatchById(id: String): Flow<Watch?>

    /**
     * Flow whether a watch with the given ID has announced the given capability.
     */
    fun watchHasCapability(watch: Watch, capability: Capability): Flow<Boolean>
}
