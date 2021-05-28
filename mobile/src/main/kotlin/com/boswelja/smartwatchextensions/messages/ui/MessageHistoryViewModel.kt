package com.boswelja.smartwatchextensions.messages.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.boswelja.smartwatchextensions.messages.database.MessageDatabase

class MessageHistoryViewModel internal constructor(
    application: Application,
    private val messageDatabase: MessageDatabase,
) : AndroidViewModel(application) {

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        MessageDatabase.getInstance(application)
    )

    val dismissedMessagesFlow = messageDatabase.messages().dismissedMessages()

    /**
     * Deletes all dismissed messages from the database, effectively clearing message history.
     */
    suspend fun clearMessageHistory() {
        messageDatabase.messages().deleteDismissed()
    }
}
