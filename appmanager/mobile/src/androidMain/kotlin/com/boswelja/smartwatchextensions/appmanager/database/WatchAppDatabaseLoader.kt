package com.boswelja.smartwatchextensions.appmanager.database

import android.content.Context
import com.squareup.sqldelight.android.AndroidSqliteDriver
import kotlinx.coroutines.Dispatchers

actual class WatchAppDatabaseLoader(private val context: Context) {
    actual fun createDatabase(): WatchAppDatabase {
        return WatchAppDatabase(
            AndroidSqliteDriver(
                schema = WatchAppDatabase.Schema,
                context = context,
                name = "watchapps.db"
            ),
            watchAppDbAdapter
        )
    }
}

actual val DB_DISPATCHER = Dispatchers.IO
