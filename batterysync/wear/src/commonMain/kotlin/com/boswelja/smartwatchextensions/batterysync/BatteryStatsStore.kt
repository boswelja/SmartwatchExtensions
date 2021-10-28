package com.boswelja.smartwatchextensions.batterysync

import kotlinx.coroutines.flow.Flow

/**
 * A class for storing and updating the battery stats of the connected phone.
 */
expect class BatteryStatsStore {

    /**
     * Flow the connected phone's [BatteryStats].
     */
    fun getStatsForPhone(): Flow<BatteryStats>

    /**
     * Update the connected phone's [BatteryStats].
     * @param newStats The new [BatteryStats] to store.
     */
    suspend fun updateStatsForPhone(newStats: BatteryStats)
}
