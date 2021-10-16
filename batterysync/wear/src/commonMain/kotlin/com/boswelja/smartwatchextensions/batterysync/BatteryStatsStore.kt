package com.boswelja.smartwatchextensions.batterysync

import kotlinx.coroutines.flow.Flow

expect class BatteryStatsStore {
    fun getStatsForPhone(): Flow<BatteryStats>
    suspend fun updateStatsForPhone(newStats: BatteryStats)
}
