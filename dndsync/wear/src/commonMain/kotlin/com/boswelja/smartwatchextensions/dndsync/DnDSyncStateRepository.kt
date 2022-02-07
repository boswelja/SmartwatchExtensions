package com.boswelja.smartwatchextensions.dndsync

import kotlinx.coroutines.flow.Flow

/**
 * A repository for managing [DnDSyncState].
 */
interface DnDSyncStateRepository {

    /**
     * Flow the [DnDSyncState].
     */
    fun getDnDSyncState(): Flow<DnDSyncState>

    /**
     * Update the [DnDSyncState].
     */
    suspend fun updateDnDSyncState(block: (DnDSyncState) -> DnDSyncState)
}
