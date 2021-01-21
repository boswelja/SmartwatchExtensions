package com.boswelja.devicemanager.widget.database

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

abstract class WatchWidgetAssociation(
    @ColumnInfo(name = "watch_id")
    val watchId: String,
    @PrimaryKey val widgetId: Int
)
