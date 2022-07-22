package com.boswelja.smartwatchextensions.batterysync.domain.repository

import com.boswelja.smartwatchextensions.batterysync.domain.model.BatterySyncConfig
import kotlinx.coroutines.flow.Flow

/**
 * A repository for managing [BatterySyncConfig].
 */
interface BatterySyncConfigRepository {

    /**
     * Flow the [BatterySyncConfig].
     */
    fun getBatterySyncState(): Flow<BatterySyncConfig>

    /**
     * Update the [BatterySyncConfig].
     */
    suspend fun updateBatterySyncState(block: (BatterySyncConfig) -> BatterySyncConfig)
}
