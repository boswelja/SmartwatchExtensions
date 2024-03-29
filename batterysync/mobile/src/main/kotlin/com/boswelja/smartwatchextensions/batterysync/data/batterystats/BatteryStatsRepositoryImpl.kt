package com.boswelja.smartwatchextensions.batterysync.data.batterystats

import android.content.Context
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.boswelja.smartwatchextensions.batterysync.BatteryStats
import com.boswelja.smartwatchextensions.batterysync.batteryStats
import com.boswelja.smartwatchextensions.batterysync.database.BatteryStatsDatabase
import com.boswelja.smartwatchextensions.batterysync.domain.repository.BatteryStatsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * An implementation of [BatteryStatsRepository] combining potentially multiple data sources.
 */
class BatteryStatsRepositoryImpl(
    private val context: Context,
    private val database: BatteryStatsDatabase,
    private val dispatcher: CoroutineContext
) : BatteryStatsRepository {

    override fun getBatteryStatsForPhone(): BatteryStats? {
        return context.batteryStats()
    }

    override fun getBatteryStatsForWatch(
        watchId: String
    ): Flow<BatteryStats?> = database
        .watchBatteryStatsQueries
        .getFor(watchId) { _, percent, charging, timestamp ->
            BatteryStats(percent.toInt(), charging, timestamp)
        }
        .asFlow()
        .mapToOneOrNull(dispatcher)

    override suspend fun deleteBatteryStatsForWatch(watchId: String) {
        withContext(dispatcher) {
            database.watchBatteryStatsQueries.removeFor(watchId)
        }
    }

    override suspend fun putBatteryStatsForWatch(
        watchId: String,
        newStats: BatteryStats
    ) {
        withContext(dispatcher) {
            database.watchBatteryStatsQueries.insert(
                watchId,
                newStats.percent.toLong(),
                newStats.charging,
                newStats.timestamp
            )
        }
    }
}
