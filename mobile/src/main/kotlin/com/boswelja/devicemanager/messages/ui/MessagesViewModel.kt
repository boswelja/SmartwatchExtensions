package com.boswelja.devicemanager.messages.ui

import android.app.Activity
import android.app.Application
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.AndroidViewModel
import com.boswelja.devicemanager.messages.database.MessageDatabase
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class MessagesViewModel @JvmOverloads constructor(
    application: Application,
    private val messageDatabase: MessageDatabase = MessageDatabase.getInstance(application),
    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(application),
    val customTabsIntent: CustomTabsIntent = CustomTabsIntent.Builder().setShowTitle(true).build()
) : AndroidViewModel(application) {

    /**
     * Gets a [kotlinx.coroutines.flow.Flow] of all active
     * [com.boswelja.devicemanager.messages.Message] instances from the database.
     */
    val activeMessagesFlow = messageDatabase.messageDao().getActiveMessages()

    /**
     * Checks for updates and starts the appropriate update flow.
     */
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

    suspend fun dismissMessage(messageId: Long) {
        withContext(Dispatchers.IO) {
            messageDatabase.messageDao().dismissMessage(messageId)
        }
    }

    suspend fun restoreMessage(messageId: Long) {
        withContext(Dispatchers.IO) {
            messageDatabase.messageDao().restoreMessage(messageId)
        }
    }
}
