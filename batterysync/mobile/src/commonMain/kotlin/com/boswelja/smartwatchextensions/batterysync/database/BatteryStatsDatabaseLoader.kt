package com.boswelja.smartwatchextensions.batterysync.database

expect class BatteryStatsDatabaseLoader {
    fun createDatabase(): BatteryStatsDatabase
}
