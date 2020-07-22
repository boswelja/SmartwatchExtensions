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

@Database(entities = [WatchBatteryStats::class], version = 2)
abstract class WatchBatteryStatsDatabase : RoomDatabase() {

    abstract fun batteryStatsDao(): BatteryStatsDao

    /**
     * Updates a [WatchBatteryStats] object in the database based on it's primary key.
     * @param watchBatteryStats The [WatchBatteryStats] object containing new data.
     */
    suspend fun updateWatchBatteryStats(watchBatteryStats: WatchBatteryStats) {
        withContext(Dispatchers.IO) {
            batteryStatsDao().updateStats(watchBatteryStats)
        }
    }

    companion object {
        /**
         * Opens an instance of [WatchBatteryStatsDatabase].
         * @param context [Context].
         * @return The newly opened [WatchBatteryStatsDatabase] instance.
         */
        fun open(context: Context): WatchBatteryStatsDatabase {
            return Room.databaseBuilder(context, WatchBatteryStatsDatabase::class.java, "battery-stats-db")
                .addMigrations(Migrations.MIGRATION_1_2)
                .build()
        }
    }
}
