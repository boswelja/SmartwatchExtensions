package com.boswelja.smartwatchextensions.batterysync.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.boswelja.smartwatchextensions.common.batterysync.BatteryStats
import com.google.android.gms.wearable.MessageEvent

@Entity(tableName = "watch_battery_stats")
data class WatchBatteryStats(
    @PrimaryKey val watchId: String,
    @ColumnInfo(name = "watch_battery_percent")
    var percent: Int,
    @ColumnInfo(name = "watch_charging")
    var isCharging: Boolean,
    @ColumnInfo(name = "last_update_time")
    var lastUpdatedMillis: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromMessage(messageEvent: MessageEvent): WatchBatteryStats {
            val watchId = messageEvent.sourceNodeId
            val batteryStats = BatteryStats.fromByteArray(messageEvent.data)
            return WatchBatteryStats(watchId, batteryStats.percent, batteryStats.isCharging)
        }
    }
}
