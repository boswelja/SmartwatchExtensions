package com.boswelja.smartwatchextensions.batterysync.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.util.UUID

@Dao
interface BatteryStatsDao {

    @Query("SELECT * FROM watch_battery_stats WHERE watchId = :watchId LIMIT 1")
    fun getStatsForWatch(watchId: UUID?): WatchBatteryStats?

    @Query("SELECT * FROM watch_battery_stats WHERE watchId = :watchId LIMIT 1")
    fun getObservableStatsForWatch(watchId: UUID): LiveData<WatchBatteryStats?>

    @Query("DELETE FROM watch_battery_stats WHERE watchId = :watchId")
    fun deleteStatsForWatch(watchId: UUID)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun updateStats(batteryStats: WatchBatteryStats)
}
