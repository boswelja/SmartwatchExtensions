package com.boswelja.smartwatchextensions.batterysync.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.boswelja.smartwatchextensions.common.batterysync.BatteryStats
import java.util.UUID

@Entity(tableName = "watch_battery_stats")
data class WatchBatteryStats(
    @PrimaryKey val watchId: UUID,
    @ColumnInfo(name = "watch_battery_percent")
    val percent: Int,
    @ColumnInfo(name = "watch_charging")
    val charging: Boolean,
    @ColumnInfo(name = "last_update_time")
    val lastUpdatedMillis: Long = System.currentTimeMillis()
) {
    companion object {
        fun BatteryStats.toWatchBatteryStats(watchId: UUID): WatchBatteryStats {
            return WatchBatteryStats(watchId, percent, charging, timestamp)
        }
    }
}
