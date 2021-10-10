package com.boswelja.smartwatchextensions.settings.database

import android.content.Context
import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.android.AndroidSqliteDriver

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
