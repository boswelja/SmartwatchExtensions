package com.boswelja.smartwatchextensions.appmanager.database

import com.squareup.sqldelight.ColumnAdapter

expect class WatchAppDatabaseLoader {
    fun createDatabase(): WatchAppDatabase
}

internal val watchAppDbAdapter = WatchAppDb.Adapter(
    permissionsAdapter = object : ColumnAdapter<List<String>, String> {
        override fun decode(databaseValue: String): List<String> =
            databaseValue.split("|")

        override fun encode(value: List<String>): String =
            value.joinToString(separator = "|")
    }
)
