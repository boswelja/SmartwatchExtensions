/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
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
import com.boswelja.devicemanager.batterysync.widget.WatchBatteryWidget
import com.boswelja.devicemanager.widget.batterysync.WatchBatteryWidgetDao
import com.boswelja.devicemanager.widget.batterysync.WatchBatteryWidgetId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Database(entities = [WatchBatteryWidgetId::class], version = 1)
abstract class WidgetDatabase : RoomDatabase() {

    abstract fun watchBatteryWidgetDao(): WatchBatteryWidgetDao

    companion object {

        suspend fun open(context: Context): WidgetDatabase {
            return withContext(Dispatchers.IO) {
                return@withContext Room.databaseBuilder(context, WidgetDatabase::class.java, "widget-db")
                        .build()
            }
        }

        suspend fun updateWatchWidgets(context: Context, watchId: String) {
            withContext(Dispatchers.IO) {
                val database = open(context)
                val widgetIds = database.watchBatteryWidgetDao().getAllForWatch(watchId).map { it.widgetId }.toIntArray()
                withContext(Dispatchers.Main) {
                    if (widgetIds.isNotEmpty()) {
                        WatchBatteryWidget.updateWidgets(context, widgetIds)
                    }
                }
                database.close()
            }
        }
    }
}
