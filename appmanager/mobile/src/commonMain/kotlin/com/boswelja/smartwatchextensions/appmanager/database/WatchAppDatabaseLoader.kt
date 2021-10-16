package com.boswelja.smartwatchextensions.appmanager.database

import kotlinx.coroutines.CoroutineDispatcher

expect class WatchAppDatabaseLoader {
    fun createDatabase(): WatchAppDatabase
}

expect val DB_DISPATCHER: CoroutineDispatcher
