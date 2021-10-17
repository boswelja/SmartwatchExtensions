package com.boswelja.smartwatchextensions.settings.database

import kotlinx.coroutines.CoroutineDispatcher

expect class WatchSettingsDatabaseLoader {
    fun createDatabase(): WatchSettingsDatabase
}

expect val DB_DISPATCHER: CoroutineDispatcher
