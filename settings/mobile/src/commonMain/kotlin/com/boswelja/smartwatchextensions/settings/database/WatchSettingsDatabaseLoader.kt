package com.boswelja.smartwatchextensions.settings.database

expect class WatchSettingsDatabaseLoader {
    fun createDatabase(): WatchSettingsDatabase
}
