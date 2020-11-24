package com.boswelja.devicemanager.messages.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.boswelja.devicemanager.messages.database.MessageDatabase
import com.boswelja.devicemanager.messages.ui.Utils.MESSAGE_PAGE_SIZE
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MessagesViewModel @JvmOverloads constructor(
    application: Application,
    private val messageDatabase: MessageDatabase = MessageDatabase.get(application),
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AndroidViewModel(application) {

    val activeMessagesPager = Pager(PagingConfig(MESSAGE_PAGE_SIZE)) {
        messageDatabase.messageDao().getActiveMessages()
    }.flow.cachedIn(viewModelScope)

    fun dismissMessage(messageId: Long) {
        viewModelScope.launch(coroutineDispatcher) {
            messageDatabase.messageDao().dismissMessage(messageId)
        }
    }
}
