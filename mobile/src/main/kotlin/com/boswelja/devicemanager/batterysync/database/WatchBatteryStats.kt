/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.batterysync.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.boswelja.devicemanager.common.batterysync.BatteryStats
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