package com.boswelja.smartwatchextensions.messages.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.boswelja.smartwatchextensions.messages.DisplayMessage
import com.boswelja.smartwatchextensions.messages.MessagesRepository
import com.boswelja.smartwatchextensions.updatechecker.UpdateChecker
import com.boswelja.smartwatchextensions.updatechecker.getUpdateChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance

class MessagesViewModel @JvmOverloads constructor(
    application: Application,
    private val updateChecker: UpdateChecker = getUpdateChecker(application)
) : AndroidViewModel(application), DIAware {

    override val di: DI by closestDI()

    private val messagesRepository: MessagesRepository by instance()

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
