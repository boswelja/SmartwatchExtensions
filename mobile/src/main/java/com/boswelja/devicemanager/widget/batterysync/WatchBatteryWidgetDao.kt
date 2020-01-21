package com.boswelja.devicemanager.widget.batterysync

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface WatchBatteryWidgetDao {

    @Query("SELECT * FROM watch_battery_widget_ids")
    fun getAll(): List<WatchBatteryWidgetId>

    @Query("SELECT * FROM watch_battery_widget_ids WHERE watch_id = :watchId")
    fun getAllForWatch(watchId: String): List<WatchBatteryWidgetId>

    @Query("SELECT * FROM watch_battery_widget_ids WHERE widgetId = :widgetId LIMIT 1")
    fun findByWidgetId(widgetId: Int): WatchBatteryWidgetId

    @Insert
    fun addWidget(watchBatteryWidgetId: WatchBatteryWidgetId)

    @Query("DELETE FROM watch_battery_widget_ids WHERE widgetId = :widgetId")
    fun removeWidget(widgetId: Int)
}