package com.boswelja.smartwatchextensions.batterysync.domain.repository

import com.boswelja.smartwatchextensions.batterysync.BatteryStats
import kotlinx.coroutines.flow.Flow

/**
 * A repository for managing [BatteryStats] for different devices.
 */
interface BatteryStatsRepository {

    /**
     * Flow the battery stats for a watch with a given ID.
     * @param watchId The ID of the watch to flow stats for.
     */
    fun getBatteryStatsForWatch(watchId: String): Flow<BatteryStats?>

    /**
     * Remove the battery stats for a watch with the given ID.
     * @param watchId The ID of the watch whose battery stats should be removed.
     */
    suspend fun deleteBatteryStatsForWatch(watchId: String)

    /**
     * Update the battery stats for a watch with the given ID.
     * @param watchId The ID of the watch whose stats should be updated.
     * @param newStats The new [BatteryStats] to store.
     */
    suspend fun putBatteryStatsForWatch(watchId: String, newStats: BatteryStats)
}
