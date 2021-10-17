package com.boswelja.smartwatchextensions.messages.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.boswelja.smartwatchextensions.messages.MessagesRepository
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance

class MessageHistoryViewModel internal constructor(
    application: Application
) : AndroidViewModel(application), DIAware {

    override val di: DI by closestDI()

    private val messagesRepository: MessagesRepository by instance()

    val dismissedMessagesFlow = messagesRepository.getAllWhere(archived = true)

    /**
     * Deletes all dismissed messages from the database, effectively clearing message history.
     */
    suspend fun clearMessageHistory() {
        messagesRepository.deleteArchived()
    }
}
