package com.boswelja.smartwatchextensions.appmanager

import android.content.Context
import com.boswelja.watchconnection.common.message.ReceivedMessage
import com.boswelja.watchconnection.serialization.MessageReceiver
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * A [MessageReceiver] for receiving [AppList] changes and updating the repository.
 */
class WatchAppReceiver : MessageReceiver<AppList>(AppListSerializer), KoinComponent {

    private val repository: WatchAppRepository by inject()

    override suspend fun onMessageReceived(context: Context, message: ReceivedMessage<AppList>) {
        val apps = message.data.mapToWatchAppDetails(message.sourceUid)
        repository.updateAll(apps)
    }
}
