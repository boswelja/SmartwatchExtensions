package com.boswelja.smartwatchextensions.settings.database

import android.content.Context
import com.squareup.sqldelight.android.AndroidSqliteDriver
import kotlinx.coroutines.Dispatchers

actual class WatchSettingsDatabaseLoader(private val context: Context) {
    actual fun createDatabase(): WatchSettingsDatabase {
        return WatchSettingsDatabase(
            AndroidSqliteDriver(
                schema = WatchSettingsDatabase.Schema,
                context = context,
                name = "watchapps.db"
            )
        )
    }
}

actual val DB_DISPATCHER = Dispatchers.IO
