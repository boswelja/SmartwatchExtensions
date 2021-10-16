package com.boswelja.smartwatchextensions.devicemanagement.database

import kotlinx.coroutines.CoroutineDispatcher

expect class RegisteredWatchDatabaseLoader {
    fun createDatabase(): RegisteredWatchDatabase
}

expect val DB_DISPATCHER: CoroutineDispatcher
