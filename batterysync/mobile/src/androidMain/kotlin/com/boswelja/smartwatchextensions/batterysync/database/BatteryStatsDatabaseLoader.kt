package com.boswelja.smartwatchextensions.batterysync.database

import android.content.Context
import com.squareup.sqldelight.android.AndroidSqliteDriver
import kotlinx.coroutines.Dispatchers

actual class BatteryStatsDatabaseLoader(private val context: Context) {
    actual fun createDatabase(): BatteryStatsDatabase {
        return BatteryStatsDatabase(
            AndroidSqliteDriver(BatteryStatsDatabase.Schema, context, "batterystats.db")
        )
    }
}

actual val DB_DISPATCHER = Dispatchers.IO
