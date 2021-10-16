package com.boswelja.smartwatchextensions.appmanager.database

import android.content.Context
import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.android.AndroidSqliteDriver

actual class WatchAppDatabaseLoader(private val context: Context) {
    actual fun createDatabase(): WatchAppDatabase {
        return WatchAppDatabase(
            AndroidSqliteDriver(
                schema = WatchAppDatabase.Schema,
                context = context,
                name = "watchapps.db"
            ),
            WatchAppDb.Adapter(
                permissionsAdapter = object : ColumnAdapter<List<String>, String> {
                    override fun decode(databaseValue: String): List<String> =
                        databaseValue.split("|")

                    override fun encode(value: List<String>): String =
                        value.joinToString(separator = "|")
                }
            )
        )
    }
}
