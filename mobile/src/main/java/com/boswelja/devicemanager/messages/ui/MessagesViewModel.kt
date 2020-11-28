package com.boswelja.devicemanager.messages.ui

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.boswelja.devicemanager.messages.MessageHandler
import com.boswelja.devicemanager.messages.database.MessageDatabase
import com.boswelja.devicemanager.messages.ui.Utils.MESSAGE_PAGE_SIZE
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import timber.log.Timber

class MessagesViewModel @JvmOverloads constructor(
    application: Application,
    private val messageDatabase: MessageDatabase = MessageDatabase.get(application),
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(application),
    private val messageHandler: MessageHandler = MessageHandler(application)
) : AndroidViewModel(application) {

    val showUndoSnackbar = messageHandler.messageDismissedEvent

    val activeMessagesPager = Pager(PagingConfig(MESSAGE_PAGE_SIZE)) {
        messageDatabase.messageDao().getActiveMessages()
    }.flow.cachedIn(viewModelScope)

    fun dismissMessage(messageId: Long) {
        messageHandler.dismissMessage(messageId)
    }

    fun restoreMessage(messageId: Long) {
        messageHandler.restoreMessage(messageId)
    }

    fun startUpdateFlow(activity: Activity) {
        appUpdateManager.appUpdateInfo.addOnCompleteListener {
            val appUpdateInfo = it.result
            if (it.isSuccessful &&
                appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
            ) {
                val options = when {
                    appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) -> {
                        AppUpdateOptions.defaultOptions(AppUpdateType.FLEXIBLE)
                    }
                    appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) -> {
                        AppUpdateOptions.defaultOptions(AppUpdateType.IMMEDIATE)
                    }
                    else -> null
                }
                options?.let { appUpdateOptions ->
                    appUpdateManager.startUpdateFlow(
                        appUpdateInfo,
                        activity,
                        appUpdateOptions
                    )
                }
            } else {
                Timber.w("Update failed")
            }
        }
    }
}
