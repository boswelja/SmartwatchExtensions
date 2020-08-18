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
import com.google.android.gms.wearable.MessageEvent

@Entity(tableName = "watch_battery_stats")
class WatchBatteryStats(
    @PrimaryKey val watchId: String,
    @ColumnInfo(name = "watch_battery_percent")
    val batteryPercent: Int,
    @ColumnInfo(name = "watch_charging")
    val isWatchCharging: Boolean,
    @ColumnInfo(name = "last_update_time")
    val lastUpdatedMillis: Long = System.currentTimeMillis()
) {
  companion object {
    fun fromMessage(messageEvent: MessageEvent): WatchBatteryStats {
      val watchId = messageEvent.sourceNodeId
      val message = String(messageEvent.data, Charsets.UTF_8)
      val messageSplit = message.split("|")
      val batteryPercent = messageSplit[0].toInt()
      val isWatchCharging = messageSplit[1] == true.toString()
      return WatchBatteryStats(watchId, batteryPercent, isWatchCharging)
    }
  }
}
