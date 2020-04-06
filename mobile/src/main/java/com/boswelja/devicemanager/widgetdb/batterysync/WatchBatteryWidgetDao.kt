/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.widgetdb.batterysync

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
    fun findByWidgetId(widgetId: Int): WatchBatteryWidgetId?

    @Insert
    fun addWidget(watchBatteryWidgetId: WatchBatteryWidgetId)

    @Query("DELETE FROM watch_battery_widget_ids WHERE widgetId = :widgetId")
    fun removeWidget(widgetId: Int)
}
