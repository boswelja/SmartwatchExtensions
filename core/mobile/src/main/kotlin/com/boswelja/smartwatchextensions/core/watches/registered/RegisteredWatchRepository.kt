package com.boswelja.smartwatchextensions.core.watches.registered

import com.boswelja.smartwatchextensions.core.devicemanagement.database.RegisteredWatch
import kotlinx.coroutines.flow.Flow

/**
 * A repository for managing registered watches, and discovering available watches.
 */
interface RegisteredWatchRepository {

    /**
     * Flows the list of registered watches.
     */
    val registeredWatches: Flow<List<RegisteredWatch>>

    /**
     * Register a new watch.
     */
    suspend fun registerWatch(id: String, name: String)

    /**
     * Deregister an existing watch.
     */
    suspend fun deregisterWatch(id: String)

    /**
     * Rename an existing watch.
     */
    suspend fun renameWatch(id: String, newName: String)

    /**
     * Flow a given watch by its ID.
     */
    fun getWatchById(id: String): Flow<RegisteredWatch?>
}
