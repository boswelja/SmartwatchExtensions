@file:Suppress("DEPRECATION")

package com.boswelja.devicemanager.bootorupdate.updater

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.room.Room
import com.boswelja.devicemanager.BuildConfig
import com.boswelja.devicemanager.appStateStore
import com.boswelja.devicemanager.widget.database.WidgetDatabase
import com.boswelja.devicemanager.widget.widgetIdStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class Updater(private val context: Context) {

    private var lastAppVersion: Int = BuildConfig.VERSION_CODE

    suspend fun checkNeedsUpdate(): Boolean {
        lastAppVersion = context.appStateStore.data.map { it.lastAppVersion }.first()
        return lastAppVersion < BuildConfig.VERSION_CODE
    }

    /**
     * Update the app's working environment.
     * @return The [Result] of the update
     */
    suspend fun doUpdate(): Result {
        var updateStatus = Result.NOT_NEEDED
        if (lastAppVersion < 2027000000) {
            updateWidgetImpl()
            updateStatus = Result.COMPLETED
        }
        return updateStatus
    }

    private suspend fun updateWidgetImpl() {
        // TODO remove this
        val widgetIdStore = context.widgetIdStore
        Room.databaseBuilder(context, WidgetDatabase::class.java, "widget-db")
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build().also { database ->
                widgetIdStore.edit { widgetIds ->
                    database.widgetDao().getAll().forEach {
                        widgetIds[stringPreferencesKey(it.widgetId.toString())] = it.watchId
                    }
                }
                database.clearAllTables()
                database.close()
            }
    }
}
