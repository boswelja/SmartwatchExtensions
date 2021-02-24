/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.widget.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.boswelja.devicemanager.common.SingletonHolder

@Database(entities = [WatchWidgetAssociation::class], version = 2)
abstract class WidgetDatabase : RoomDatabase() {

    abstract fun widgetDao(): WidgetDao

    /**
     * Add a [WatchWidgetAssociation] to the database.
     * @param widgetId The ID of the widget to associate with a watch.
     * @param watchId The ID of the watch to associate with a widget.
     */
    fun addWidget(widgetId: Int, watchId: String) {
        widgetDao().addWidget(WatchWidgetAssociation(watchId, widgetId))
    }

    /**
     * Remove a [WatchWidgetAssociation] from the database.
     * @param widgetId The ID of the widget to find and remove the corresponding
     * [WatchWidgetAssociation].
     */
    fun removeWidget(widgetId: Int) {
        widgetDao().removeWidget(widgetId)
    }

    /**
     * Gets the associated watch ID for a given widget.
     * @param widgetId The ID of the widget to get the associated watch ID for.
     * @return The ID of the associated watch.
     */
    fun getWatchIDForWidget(widgetId: Int): String {
        return widgetDao().findByWidgetId(widgetId).watchId
    }

    fun getAllWidgetsForWatch(watchId: String): List<Int> {
        return widgetDao().getAllForWatch(watchId)
            .map {
                it.widgetId
            }
    }

    companion object : SingletonHolder<WidgetDatabase, Context>({ context ->
        Room.databaseBuilder(
            context, WidgetDatabase::class.java, "widget-db"
        ).apply {
            addMigrations(Migrations.MIGRATION_1_2)
            fallbackToDestructiveMigration()
        }.build()
    })
}
