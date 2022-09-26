package com.boswelja.smartwatchextensions.appmanager

import android.content.Context
import com.boswelja.watchconnection.common.message.MessageReceiver
import com.boswelja.watchconnection.common.message.ReceivedMessage
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * A [MessageReceiver] for receiving [AppList] changes and updating the repository.
 */
class RemovedAppReceiver : MessageReceiver(), KoinComponent {

    private val repository: WatchAppRepository by inject()
    private val iconRepository: WatchAppIconRepository by inject()

    override suspend fun onMessageReceived(context: Context, message: ReceivedMessage<ByteArray?>) {
        if (message.path == RemovedAppsList) {
            val packages = RemovedAppsSerializer.deserialize(message.data)
            val watchId = message.sourceUid
            repository.delete(watchId, packages.packages)

            packages.packages.forEach {
                iconRepository.removeIconFor(watchId, it)
            }
        }
    }
}
