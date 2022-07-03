package com.boswelja.smartwatchextensions.batterysync.data.repository

import com.boswelja.smartwatchextensions.batterysync.BatteryStats
import com.boswelja.smartwatchextensions.batterysync.data.local.WatchBatteryStatsDbDataSource
import com.boswelja.smartwatchextensions.batterysync.domain.repository.BatteryStatsRepository
import kotlinx.coroutines.flow.Flow

/**
 * An implementation of [BatteryStatsRepository] combining potentially multiple data sources.
 */
class BatteryStatsRepositoryImpl(
    private val dbDataSource: WatchBatteryStatsDbDataSource
) : BatteryStatsRepository {

    override fun getBatteryStatsForWatch(watchId: String): Flow<BatteryStats?> = dbDataSource.batteryStatsFor(watchId)

    override suspend fun deleteBatteryStatsForWatch(watchId: String) = dbDataSource.removeStatsFor(watchId)

    override suspend fun putBatteryStatsForWatch(
        watchId: String,
        newStats: BatteryStats
    ) = dbDataSource.updateStatsFor(watchId, newStats)
}
