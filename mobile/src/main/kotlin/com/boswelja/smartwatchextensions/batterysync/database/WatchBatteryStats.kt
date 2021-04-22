package com.boswelja.smartwatchextensions.batterysync.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.boswelja.smartwatchextensions.common.batterysync.BatteryStats
import com.google.android.gms.wearable.MessageEvent
import java.util.UUID

@Entity(tableName = "watch_battery_stats")
data class WatchBatteryStats(
    @PrimaryKey val watchId: UUID,
    @ColumnInfo(name = "watch_battery_percent")
    var percent: Int,
    @ColumnInfo(name = "watch_charging")
    var isCharging: Boolean,
    @ColumnInfo(name = "last_update_time")
    var lastUpdatedMillis: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromMessage(watchId: UUID, messageEvent: MessageEvent): WatchBatteryStats {
            val batteryStats = BatteryStats.fromByteArray(messageEvent.data)
            return WatchBatteryStats(watchId, batteryStats.percent, batteryStats.isCharging)
        }
    }
}
