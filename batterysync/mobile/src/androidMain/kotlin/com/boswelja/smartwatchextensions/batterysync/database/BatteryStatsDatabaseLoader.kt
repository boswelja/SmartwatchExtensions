package com.boswelja.smartwatchextensions.batterysync.database

import android.content.Context
import com.squareup.sqldelight.android.AndroidSqliteDriver

actual class BatteryStatsDatabaseLoader(private val context: Context) {
    actual fun createDatabase(): BatteryStatsDatabase {
        return BatteryStatsDatabase(
            AndroidSqliteDriver(BatteryStatsDatabase.Schema, context, "batterystats.db")
        )
    }
}
