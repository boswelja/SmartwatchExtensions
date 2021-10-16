package com.boswelja.smartwatchextensions.messages.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.boswelja.smartwatchextensions.messages.DisplayMessage
import com.boswelja.smartwatchextensions.messages.MessagesDbRepository
import com.boswelja.smartwatchextensions.messages.MessagesRepository
import com.boswelja.smartwatchextensions.messages.database.MessagesDatabaseLoader
import com.boswelja.smartwatchextensions.updatechecker.UpdateChecker
import com.boswelja.smartwatchextensions.updatechecker.getUpdateChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class MessagesViewModel @JvmOverloads constructor(
    application: Application,
    private val messagesRepository: MessagesRepository =
        MessagesDbRepository(MessagesDatabaseLoader(application).createDatabase()),
    private val updateChecker: UpdateChecker = getUpdateChecker(application)
) : AndroidViewModel(application) {

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
