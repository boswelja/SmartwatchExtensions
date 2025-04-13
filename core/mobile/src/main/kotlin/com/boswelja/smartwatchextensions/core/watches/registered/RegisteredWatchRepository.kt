package com.boswelja.smartwatchextensions.core.watches.registered

import com.boswelja.smartwatchextensions.core.watches.Watch
import kotlinx.coroutines.flow.Flow

/**
 * A repository for managing registered watches, and discovering available watches.
 */
interface RegisteredWatchRepository {

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
     * Flow a given [Watch] by it's [Watch.uid].
     */
    fun getWatchById(id: String): Flow<Watch?>
}
