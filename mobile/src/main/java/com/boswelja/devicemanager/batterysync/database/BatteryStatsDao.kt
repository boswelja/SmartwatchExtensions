/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.batterysync.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BatteryStatsDao {

  @Query("SELECT * FROM watch_battery_stats WHERE watchId = :watchId LIMIT 1")
  fun getStatsForWatch(watchId: String): WatchBatteryStats?

  @Query("SELECT * FROM watch_battery_stats WHERE watchId = :watchId LIMIT 1")
  fun getObservableStatsForWatch(watchId: String): LiveData<WatchBatteryStats?>

  @Query("DELETE FROM watch_battery_stats WHERE watchId = :watchId")
  fun deleteStatsForWatch(watchId: String)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun updateStats(batteryStats: WatchBatteryStats)
}
