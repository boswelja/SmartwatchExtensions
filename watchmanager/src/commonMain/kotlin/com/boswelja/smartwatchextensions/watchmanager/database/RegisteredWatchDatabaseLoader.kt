package com.boswelja.smartwatchextensions.watchmanager.database

expect class RegisteredWatchDatabaseLoader {
    fun createDatabase(): RegisteredWatchDatabase
}