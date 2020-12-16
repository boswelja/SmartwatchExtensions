package com.boswelja.devicemanager.messages.ui

import android.app.Activity
import android.app.Application
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.DataEvent
import com.boswelja.devicemanager.messages.database.MessageDatabase
import com.boswelja.devicemanager.messages.ui.Utils.MESSAGE_PAGE_SIZE
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class MessagesViewModel @JvmOverloads constructor(
    application: Application,
    private val messageDatabase: MessageDatabase = MessageDatabase.get(application),
    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(application),
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : AndroidViewModel(application) {

    private val customTabsIntent: CustomTabsIntent by lazy {
        CustomTabsIntent.Builder().setShowTitle(true).build()
    }

    val messageDismissedEvent = DataEvent<Long>()

    val activeMessagesPager = Pager(PagingConfig(MESSAGE_PAGE_SIZE)) {
        messageDatabase.messageDao().getActiveMessages()
    }.flow.cachedIn(viewModelScope)

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

    fun dismissMessage(messageId: Long) {
        coroutineScope.launch {
            messageDatabase.messageDao().dismissMessage(messageId)
            messageDismissedEvent.postValue(messageId)
        }
    }

    fun restoreMessage(messageId: Long) {
        coroutineScope.launch { messageDatabase.messageDao().restoreMessage(messageId) }
    }

    fun showChangelog() {
        customTabsIntent.launchUrl(
            getApplication(),
            getApplication<Application>().getString(R.string.changelog_url).toUri()
        )
    }
}
