package com.boswelja.smartwatchextensions.batterysync.data.repository

import com.boswelja.smartwatchextensions.batterysync.BatteryStats
import com.boswelja.smartwatchextensions.batterysync.data.local.PhoneBatteryStatsDataSource
import com.boswelja.smartwatchextensions.batterysync.data.local.WatchBatteryStatsDbDataSource
import com.boswelja.smartwatchextensions.batterysync.domain.repository.BatteryStatsRepository
import kotlinx.coroutines.flow.Flow

/**
 * An implementation of [BatteryStatsRepository] combining potentially multiple data sources.
 */
class BatteryStatsRepositoryImpl(
    private val watchDbDataSource: WatchBatteryStatsDbDataSource,
    private val phoneDataSource: PhoneBatteryStatsDataSource
) : BatteryStatsRepository {

    override fun getBatteryStatsForPhone(): BatteryStats? {
        return phoneDataSource.getBatteryStats()
    }

    override fun getBatteryStatsForWatch(watchId: String): Flow<BatteryStats?> = watchDbDataSource.batteryStatsFor(watchId)

    override suspend fun deleteBatteryStatsForWatch(watchId: String) = watchDbDataSource.removeStatsFor(watchId)

    override suspend fun putBatteryStatsForWatch(
        watchId: String,
        newStats: BatteryStats
    ) = watchDbDataSource.updateStatsFor(watchId, newStats)
}
