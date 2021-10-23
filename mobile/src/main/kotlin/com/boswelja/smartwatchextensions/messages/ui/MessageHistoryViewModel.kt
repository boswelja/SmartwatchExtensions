package com.boswelja.smartwatchextensions.messages.ui

import androidx.lifecycle.ViewModel
import com.boswelja.smartwatchextensions.messages.MessagesRepository

class MessageHistoryViewModel(
    private val messagesRepository: MessagesRepository
) : ViewModel() {

    val dismissedMessagesFlow = messagesRepository.getAllWhere(archived = true)

    /**
     * Deletes all dismissed messages from the database, effectively clearing message history.
     */
    suspend fun clearMessageHistory() {
        messagesRepository.deleteArchived()
    }
}
