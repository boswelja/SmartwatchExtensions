package com.boswelja.smartwatchextensions.appmanager

import android.content.Context
import com.boswelja.watchconection.common.message.MessageReceiver
import com.boswelja.watchconnection.common.message.ReceivedMessage
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WatchAppReceiver : MessageReceiver<AppList>(AppListSerializer), KoinComponent {

    private val repository: WatchAppRepository by inject()

    override suspend fun onMessageReceived(context: Context, message: ReceivedMessage<AppList>) {
        val apps = message.data.mapToWatchAppDetails(message.sourceUid)
        repository.updateAll(apps)
    }
}
