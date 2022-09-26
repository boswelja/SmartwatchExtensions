package com.boswelja.smartwatchextensions.appmanager

import android.content.Context
import com.boswelja.watchconnection.common.message.MessageReceiver
import com.boswelja.watchconnection.common.message.ReceivedMessage
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * A [MessageReceiver] for receiving [AppList] changes and updating the repository.
 */
class AddedOrUpdatedAppReceiver : MessageReceiver(), KoinComponent {

    private val repository: WatchAppRepository by inject()

    override suspend fun onMessageReceived(context: Context, message: ReceivedMessage<ByteArray?>) {
        if (message.path == AddedAppsList || message.path == UpdatedAppsList) {
            val appList = message.data?.let { AddedOrUpdatedAppsSerializer.deserialize(it) } ?: return
            val apps = appList.mapToWatchAppDetails(message.sourceUid)
            repository.updateAll(apps)
        }
    }
}
