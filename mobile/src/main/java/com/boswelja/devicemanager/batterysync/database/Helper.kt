/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.batterysync.database

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object Helper {

    suspend fun openDatabase(context: Context): WatchBatteryStatsDatabase {
        return withContext(Dispatchers.IO) {
            return@withContext Room.databaseBuilder(context, WatchBatteryStatsDatabase::class.java, "battery-stats-db")
                    .build()
        }
    }

    suspend fun updateWatchBatteryStats(database: WatchBatteryStatsDatabase, watchId: String, batteryPercent: Int) {
        withContext(Dispatchers.IO) {
            val updateTime = System.currentTimeMillis()
            val watchBatteryStats = WatchBatteryStats(watchId, batteryPercent, updateTime)
            database.batteryStatsDao().updateStats(watchBatteryStats)
        }
    }
}
