package com.boswelja.devicemanager.messages.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.boswelja.devicemanager.messages.database.MessageDatabase
import com.boswelja.devicemanager.messages.ui.Utils.MESSAGE_PAGE_SIZE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MessageHistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val messageDatabase = MessageDatabase.get(application)

    val dismissedMessagesPager = Pager(PagingConfig(MESSAGE_PAGE_SIZE)) {
        messageDatabase.messageDao().getDismissedMessages()
    }.flow.cachedIn(viewModelScope)

    /**
     * Deletes all dismissed messages from the database, effectively clearing message history.
     */
    fun clearMessageHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            messageDatabase.messageDao().clearDismissedMessages()
        }
    }
}
