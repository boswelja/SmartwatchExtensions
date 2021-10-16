package com.boswelja.smartwatchextensions.devicemanagement.database

import android.content.Context
import com.squareup.sqldelight.android.AndroidSqliteDriver

actual class RegisteredWatchDatabaseLoader(private val context: Context) {
    actual fun createDatabase(): RegisteredWatchDatabase {
        return RegisteredWatchDatabase(
            AndroidSqliteDriver(
                schema = RegisteredWatchDatabase.Schema,
                context = context,
                name = "registeredwatches.db"
            )
        )
    }
}
