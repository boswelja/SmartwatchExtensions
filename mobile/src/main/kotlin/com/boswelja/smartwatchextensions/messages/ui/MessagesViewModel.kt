package com.boswelja.smartwatchextensions.messages.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import com.boswelja.smartwatchextensions.messages.DisplayMessage
import com.boswelja.smartwatchextensions.messages.MessagesRepository
import com.boswelja.smartwatchextensions.updatechecker.UpdateChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class MessagesViewModel(
    private val messagesRepository: MessagesRepository,
    private val updateChecker: UpdateChecker
) : ViewModel() {

    val activeMessagesFlow: Flow<List<DisplayMessage>> =
        messagesRepository.getAllWhere(archived = false)

    /**
     * See [UpdateChecker.launchDownloadScreen].
     */
    fun startUpdateFlow(context: Context) {
        updateChecker.launchDownloadScreen(context)
    }

    suspend fun dismissMessage(messageId: Long) {
        withContext(Dispatchers.IO) {
            messagesRepository.archive(messageId)
        }
    }
}
