package com.boswelja.smartwatchextensions.appmanager

import android.content.Context
import com.boswelja.watchconnection.common.message.ReceivedMessage
import com.boswelja.watchconnection.serialization.MessageReceiver
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * A [MessageReceiver] for receiving [AppList] changes and updating the repository.
 */
class RemovedAppReceiver : MessageReceiver<RemovedApps>(RemovedAppsSerializer), KoinComponent {

    private val repository: WatchAppRepository by inject()
    private val iconRepository: WatchAppIconRepository by inject()

    override suspend fun onMessageReceived(context: Context, message: ReceivedMessage<RemovedApps>) {
        val watchId = message.sourceUid
        val packages = message.data.packages
        repository.delete(watchId, packages)

        packages.forEach {
            iconRepository.removeIconFor(watchId, it)
        }
    }
}
