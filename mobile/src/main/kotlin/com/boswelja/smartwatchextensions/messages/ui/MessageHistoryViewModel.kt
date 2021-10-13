package com.boswelja.smartwatchextensions.messages.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.boswelja.smartwatchextensions.messages.MessagesDbRepository
import com.boswelja.smartwatchextensions.messages.MessagesRepository
import com.boswelja.smartwatchextensions.messages.database.MessagesDatabaseLoader

class MessageHistoryViewModel internal constructor(
    application: Application,
    private val messagesRepository: MessagesRepository,
) : AndroidViewModel(application) {

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        MessagesDbRepository(MessagesDatabaseLoader(application).createDatabase())
    )

    val dismissedMessagesFlow = messagesRepository.getAllWhere(archived = true)

    /**
     * Deletes all dismissed messages from the database, effectively clearing message history.
     */
    suspend fun clearMessageHistory() {
        messagesRepository.deleteArchived()
    }
}
