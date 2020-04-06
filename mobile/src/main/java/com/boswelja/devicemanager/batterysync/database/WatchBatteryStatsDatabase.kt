/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.batterysync.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Database(entities = [WatchBatteryStats::class], version = 1)
abstract class WatchBatteryStatsDatabase : RoomDatabase() {

    abstract fun batteryStatsDao(): BatteryStatsDao

    suspend fun updateWatchBatteryStats(watchId: String, batteryPercent: Int) {
        withContext(Dispatchers.IO) {
            val updateTime = System.currentTimeMillis()
            val watchBatteryStats = WatchBatteryStats(watchId, batteryPercent, updateTime)
            batteryStatsDao().updateStats(watchBatteryStats)
        }
    }

    companion object {
        suspend fun open(context: Context): WatchBatteryStatsDatabase {
            return withContext(Dispatchers.IO) {
                return@withContext Room.databaseBuilder(context, WatchBatteryStatsDatabase::class.java, "battery-stats-db")
                        .build()
            }
        }
    }
}
