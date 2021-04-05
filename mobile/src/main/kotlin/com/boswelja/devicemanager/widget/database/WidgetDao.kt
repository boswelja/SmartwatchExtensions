package com.boswelja.devicemanager.widget.database

import androidx.room.Dao
import androidx.room.Query

@Dao
interface WidgetDao {
    @Query("SELECT * FROM watch_widget_associations")
    fun getAll(): List<WatchWidgetAssociation>
}
