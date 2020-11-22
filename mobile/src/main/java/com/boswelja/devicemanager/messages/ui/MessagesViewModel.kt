package com.boswelja.devicemanager.messages.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.boswelja.devicemanager.messages.database.MessageDatabase

class MessagesViewModel(application: Application) : AndroidViewModel(application) {

    private val messageDatabase = MessageDatabase.get(application)

    val activeMessagesPager = Pager(PagingConfig(MESSAGE_PAGE_SIZE)) {
        messageDatabase.messageDao().getActiveMessages()
    }.flow.cachedIn(viewModelScope)
    val dismissedMessagesPager by lazy {
        Pager(PagingConfig(MESSAGE_PAGE_SIZE)) {
            messageDatabase.messageDao().getDismissedMessages()
        }.flow.cachedIn(viewModelScope)
    }

    companion object {
        private const val MESSAGE_PAGE_SIZE = 20
    }
}
