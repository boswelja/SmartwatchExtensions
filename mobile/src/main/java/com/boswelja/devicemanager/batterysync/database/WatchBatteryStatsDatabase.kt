package com.boswelja.devicemanager.batterysync.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [WatchBatteryStats::class], version = 1)
abstract class WatchBatteryStatsDatabase : RoomDatabase() {

    abstract fun batteryStatsDao(): BatteryStatsDao

}
