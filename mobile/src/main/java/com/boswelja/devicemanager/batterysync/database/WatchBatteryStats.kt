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

@Entity(tableName = "watch_battery_stats")
class WatchBatteryStats(
    @PrimaryKey val watchId: String,
    @ColumnInfo(name = "watch_battery_percent") val batteryPercent: Int,
    @ColumnInfo(name = "last_update_time") val lastUpdatedMillis: Long
)
