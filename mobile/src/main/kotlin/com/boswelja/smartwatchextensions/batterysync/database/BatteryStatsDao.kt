package com.boswelja.smartwatchextensions.batterysync.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.util.UUID
import kotlinx.coroutines.flow.Flow

@Dao
interface BatteryStatsDao {

    @Query("SELECT * FROM watch_battery_stats WHERE watchId = :watchId LIMIT 1")
    fun getStats(watchId: String?): Flow<WatchBatteryStats>

    @Query("DELETE FROM watch_battery_stats WHERE watchId = :watchId")
    suspend fun deleteStats(watchId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateStats(batteryStats: WatchBatteryStats)
}
