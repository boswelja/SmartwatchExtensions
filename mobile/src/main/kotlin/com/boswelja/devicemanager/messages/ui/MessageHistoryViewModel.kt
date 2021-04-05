package com.boswelja.devicemanager.messages.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.boswelja.devicemanager.messages.database.MessageDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MessageHistoryViewModel internal constructor(
    application: Application,
    private val messageDatabase: MessageDatabase,
    private val coroutineDispatcher: CoroutineDispatcher
) : AndroidViewModel(application) {

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        MessageDatabase.getInstance(application),
        Dispatchers.IO
    )

    val dismissedMessagesFlow = messageDatabase.messageDao().getDismissedMessages()

    /**
     * Deletes all dismissed messages from the database, effectively clearing message history.
     */
    suspend fun clearMessageHistory() {
        withContext(coroutineDispatcher) {
            messageDatabase.clearMessageHistory()
        }
    }
}
