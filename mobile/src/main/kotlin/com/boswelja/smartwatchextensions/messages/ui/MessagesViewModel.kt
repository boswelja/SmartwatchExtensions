package com.boswelja.smartwatchextensions.messages.ui

import android.app.Application
import android.content.Context
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.AndroidViewModel
import com.boswelja.smartwatchextensions.messages.database.MessageDatabase
import com.boswelja.smartwatchextensions.updatechecker.UpdateChecker
import com.boswelja.smartwatchextensions.updatechecker.getUpdateChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MessagesViewModel @JvmOverloads constructor(
    application: Application,
    private val messageDatabase: MessageDatabase = MessageDatabase.getInstance(application),
    private val updateChecker: UpdateChecker = getUpdateChecker(application),
    val customTabsIntent: CustomTabsIntent = CustomTabsIntent.Builder().setShowTitle(true).build()
) : AndroidViewModel(application) {

    /**
     * Gets a [kotlinx.coroutines.flow.Flow] of all active
     * [com.boswelja.smartwatchextensions.messages.Message] instances from the database.
     */
    val activeMessagesFlow = messageDatabase.messages().activeMessages()

    /**
     * See [UpdateChecker.launchDownloadScreen].
     */
    fun startUpdateFlow(context: Context) {
        updateChecker.launchDownloadScreen(context)
    }

    suspend fun dismissMessage(messageId: Long) {
        withContext(Dispatchers.IO) {
            messageDatabase.messages().dismiss(messageId)
        }
    }

    suspend fun restoreMessage(messageId: Long) {
        withContext(Dispatchers.IO) {
            messageDatabase.messages().restore(messageId)
        }
    }
}
