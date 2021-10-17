package com.boswelja.smartwatchextensions.messages.database

import kotlinx.coroutines.CoroutineDispatcher

expect class MessagesDatabaseLoader {
    fun createDatabase(): MessageDatabase
}

expect val DB_DISPATCHER: CoroutineDispatcher
