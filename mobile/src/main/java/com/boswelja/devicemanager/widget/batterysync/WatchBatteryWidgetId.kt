/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.widget.batterysync

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watch_battery_widget_ids")
class WatchBatteryWidgetId(
    @ColumnInfo(name = "watch_id") val watchId: String,
    @PrimaryKey val widgetId: Int
)
