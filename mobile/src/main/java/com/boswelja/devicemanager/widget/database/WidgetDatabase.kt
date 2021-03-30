package com.boswelja.devicemanager.widget.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Deprecated("Switch to DataStore implementation")
@Database(entities = [WatchWidgetAssociation::class], version = 2)
abstract class WidgetDatabase : RoomDatabase() {

    abstract fun widgetDao(): WidgetDao
}
