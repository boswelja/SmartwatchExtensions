/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.widget.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface WidgetDao {

    @Query("SELECT * FROM watch_widget_associations WHERE watch_id = :watchId")
    fun getAllForWatch(watchId: String): List<WatchWidgetAssociation>

    @Query("SELECT * FROM watch_widget_associations WHERE widgetId = :widgetId LIMIT 1")
    fun findByWidgetId(widgetId: Int): WatchWidgetAssociation

    @Insert
    fun addWidget(watchWidgetAssociation: WatchWidgetAssociation)

    @Query("DELETE FROM watch_widget_associations WHERE widgetId = :widgetId")
    fun removeWidget(widgetId: Int)

    @Query("DELETE FROM watch_widget_associations WHERE watch_id = :watchId")
    fun removeWidgetsFor(watchId: String)
}
