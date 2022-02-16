package com.boswelja.smartwatchextensions.messages.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import com.boswelja.smartwatchextensions.core.UpdateChecker
import com.boswelja.smartwatchextensions.messages.DisplayMessage
import com.boswelja.smartwatchextensions.messages.MessagesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * A ViewModel for providing data to Messages.
 */
class MessagesViewModel(
    private val messagesRepository: MessagesRepository,
    private val updateChecker: UpdateChecker
) : ViewModel() {

    /**
     * Flow a list of currently active messages.
     */
    val activeMessagesFlow: Flow<List<DisplayMessage>> =
        messagesRepository.getAllWhere(archived = false)

    /**
     * See [UpdateChecker.launchDownloadScreen].
     */
    fun startUpdateFlow(context: Context) {
        updateChecker.launchDownloadScreen(context)
    }

    /**
     * Move a message to the archive.
     */
    suspend fun dismissMessage(messageId: Long) {
        withContext(Dispatchers.IO) {
            messagesRepository.archive(messageId)
        }
    }
}
