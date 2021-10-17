package com.boswelja.smartwatchextensions.batterysync.database

import kotlinx.coroutines.CoroutineDispatcher

expect class BatteryStatsDatabaseLoader {
    fun createDatabase(): BatteryStatsDatabase
}

expect val DB_DISPATCHER: CoroutineDispatcher
