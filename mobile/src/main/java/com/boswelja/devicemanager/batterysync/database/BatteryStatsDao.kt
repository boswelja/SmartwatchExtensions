package com.boswelja.devicemanager.batterysync.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BatteryStatsDao {

    @Query("SELECT * FROM watch_battery_stats WHERE watchId = :watchId LIMIT 1")
    fun getStatsForWatch(watchId: String): WatchBatteryStats?

    @Query("DELETE FROM watch_battery_stats WHERE watchId = :watchId")
    fun deleteStatsForWatch(watchId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun updateStats(batteryStats: WatchBatteryStats)
}
