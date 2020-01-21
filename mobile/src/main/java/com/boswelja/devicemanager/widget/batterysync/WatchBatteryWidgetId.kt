package com.boswelja.devicemanager.widget.batterysync

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watch_battery_widget_ids")
class WatchBatteryWidgetId(
        @ColumnInfo(name = "watch_id") val watchId: String,
        @PrimaryKey val widgetId: Int
)