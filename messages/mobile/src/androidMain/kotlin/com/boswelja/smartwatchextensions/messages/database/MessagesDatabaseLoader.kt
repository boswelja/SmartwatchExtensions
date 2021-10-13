package com.boswelja.smartwatchextensions.messages.database

import android.content.Context
import com.squareup.sqldelight.EnumColumnAdapter
import com.squareup.sqldelight.android.AndroidSqliteDriver

actual class MessagesDatabaseLoader(private val context: Context) {
    actual fun createDatabase(): MessageDatabase {
        return MessageDatabase(
            AndroidSqliteDriver(
                schema = MessageDatabase.Schema,
                context = context,
                name = "messages.db"
            ),
            MessageDb.Adapter(
                iconAdapter = EnumColumnAdapter(),
                actionAdapter = EnumColumnAdapter()
            )
        )
    }
}
