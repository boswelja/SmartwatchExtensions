package com.boswelja.smartwatchextensions.batterysync.domain

import kotlinx.coroutines.flow.Flow

/**
 * A repository for managing [BatterySyncState].
 */
interface BatterySyncStateRepository {

    /**
     * Flow the [BatterySyncState].
     */
    fun getBatterySyncState(): Flow<BatterySyncState>

    /**
     * Update the [BatterySyncState].
     */
    suspend fun updateBatterySyncState(block: (BatterySyncState) -> BatterySyncState)
}
