package com.boswelja.devicemanager.widget.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Data class holding a [com.boswelja.devicemanager.watchmanager.item.Watch.id] and a widget ID
 * for associating a widget with a watch.
 */
@Entity(tableName = "watch_widget_associations")
data class WatchWidgetAssociation(
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
