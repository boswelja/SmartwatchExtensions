package com.boswelja.smartwatchextensions.core.devicemanagement

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
     * Get a list of all capabilities declared by a given [Watch].
     */
    suspend fun getCapabilitiesFor(watch: Watch): Set<String>

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
    fun watchHasCapability(watch: Watch, capability: String): Flow<Boolean>
}
