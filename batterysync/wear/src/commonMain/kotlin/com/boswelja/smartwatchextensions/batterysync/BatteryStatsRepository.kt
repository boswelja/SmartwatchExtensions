package com.boswelja.smartwatchextensions.batterysync

import kotlinx.coroutines.flow.Flow

/**
 * A repository for storing and loading [BatteryStats] for the paired phone.
 */
interface BatteryStatsRepository {

    /**
     * Flow the paired phone's [BatteryStats].
     */
    fun getPhoneBatteryStats(): Flow<BatteryStats>

    /**
     * Updated the stored [BatteryStats] for the paired phone.
     */
    suspend fun updatePhoneBatteryStats(newStats: BatteryStats)
}
