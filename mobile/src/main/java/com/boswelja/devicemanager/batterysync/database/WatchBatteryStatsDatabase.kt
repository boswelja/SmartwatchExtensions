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

@Database(entities = [WatchBatteryStats::class], version = 2)
abstract class WatchBatteryStatsDatabase : RoomDatabase() {

    abstract fun batteryStatsDao(): BatteryStatsDao

    override fun close() {
        INSTANCE = null
        super.close()
    }

    companion object {
        private var INSTANCE: WatchBatteryStatsDatabase? = null

        /**
         * Gets an instance of [WatchBatteryStatsDatabase].
         * @param context [Context].
         * @return The [WatchBatteryStatsDatabase] instance.
         */
        fun get(context: Context): WatchBatteryStatsDatabase {
            synchronized(this) {
                if (INSTANCE == null) {
                    INSTANCE =
                        Room.databaseBuilder(
                                context, WatchBatteryStatsDatabase::class.java, "battery-stats-db")
                            .addMigrations(Migrations.MIGRATION_1_2)
                            .build()
                }
                return INSTANCE!!
            }
        }
    }
}
