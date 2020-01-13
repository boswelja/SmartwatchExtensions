package com.boswelja.devicemanager.batterysync.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watch_battery_stats")
class WatchBatteryStats(
        @PrimaryKey val watchId: String,
        @ColumnInfo(name = "watch_battery_percent") val batteryPercent: Int,
        @ColumnInfo(name = "last_update_time") val lastUpdatedMillis: Long
)