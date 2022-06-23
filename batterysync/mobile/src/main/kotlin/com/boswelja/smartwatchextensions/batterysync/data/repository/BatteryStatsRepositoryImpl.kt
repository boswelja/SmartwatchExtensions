package com.boswelja.smartwatchextensions.batterysync.data.repository

import com.boswelja.smartwatchextensions.batterysync.BatteryStats
import com.boswelja.smartwatchextensions.batterysync.data.local.BatteryStatsDbDataSource
import com.boswelja.smartwatchextensions.batterysync.domain.repository.BatteryStatsRepository
import kotlinx.coroutines.flow.Flow

/**
 * An implementation of [BatteryStatsRepository] combining potentially multiple data sources.
 */
class BatteryStatsRepositoryImpl(
    private val dbDataSource: BatteryStatsDbDataSource
) : BatteryStatsRepository {

    override fun batteryStatsFor(watchId: String): Flow<BatteryStats?> = dbDataSource.batteryStatsFor(watchId)

    override suspend fun removeStatsFor(watchId: String) = dbDataSource.removeStatsFor(watchId)

    override suspend fun updateStatsFor(
        watchId: String,
        newStats: BatteryStats
    ) = dbDataSource.updateStatsFor(watchId, newStats)
}
