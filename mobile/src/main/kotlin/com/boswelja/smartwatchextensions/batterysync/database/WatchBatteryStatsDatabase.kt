package com.boswelja.smartwatchextensions.batterysync.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.boswelja.smartwatchextensions.common.SingletonHolder

@Database(entities = [WatchBatteryStats::class], version = 2)
abstract class WatchBatteryStatsDatabase : RoomDatabase() {

    abstract fun batteryStatsDao(): BatteryStatsDao

    companion object : SingletonHolder<WatchBatteryStatsDatabase, Context>({ context ->
        Room.databaseBuilder(
            context, WatchBatteryStatsDatabase::class.java, "battery-stats-db"
        ).apply {
            addMigrations(Migrations.MIGRATION_1_2)
            fallbackToDestructiveMigration()
        }.build()
    })
}
