package com.boswelja.devicemanager.widget.database

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

abstract class WatchWidgetAssociation(
    @ColumnInfo(name = "watch_id")
    val watchId: String,
    @PrimaryKey val widgetId: Int
) {
    override fun equals(other: Any?): Boolean {
        if (other !is WatchWidgetAssociation) return false
        return other.watchId == watchId && other.widgetId == widgetId
    }

    override fun hashCode(): Int {
        var result = watchId.hashCode()
        result = 31 * result + widgetId
        return result
    }
}
