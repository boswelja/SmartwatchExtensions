package com.boswelja.smartwatchextensions.batterysync.data.local

import com.boswelja.smartwatchextensions.batterysync.BatteryStats
import com.boswelja.smartwatchextensions.batterysync.database.BatteryStatsDatabase
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToOneOrNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class WatchBatteryStatsDbDataSource(
    private val database: BatteryStatsDatabase,
    private val dispatcher: CoroutineContext
) {

    /**
     * Flow the battery stats for a watch with a given ID.
     * @param watchId The ID of the watch to flow stats for.
     */
    fun batteryStatsFor(watchId: String): Flow<BatteryStats?> = database
        .watchBatteryStatsQueries
        .getFor(watchId) { _, percent, charging, timestamp ->
            BatteryStats(percent, charging, timestamp)
        }
        .asFlow()
        .mapToOneOrNull()

    /**
     * Remove the battery stats for a watch with the given ID.
     * @param watchId The ID of the watch whose battery stats should be removed.
     */
    suspend fun removeStatsFor(watchId: String) {
        withContext(dispatcher) {
            database.watchBatteryStatsQueries.removeFor(watchId)
        }
    }

    /**
     * Update the battery stats for a watch with the given ID.
     * @param watchId The ID of the watch whose stats should be updated.
     * @param newStats The new [BatteryStats] to store.
     */
    suspend fun updateStatsFor(watchId: String, newStats: BatteryStats) {
        withContext(dispatcher) {
            database.watchBatteryStatsQueries.insert(
                watchId,
                newStats.percent,
                newStats.charging,
                newStats.timestamp
            )
        }
    }
}