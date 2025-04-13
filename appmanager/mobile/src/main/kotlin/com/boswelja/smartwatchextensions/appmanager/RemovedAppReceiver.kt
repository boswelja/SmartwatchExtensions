package com.boswelja.smartwatchextensions.appmanager

import android.content.Context
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * A [WearableListenerService] for receiving [AppList] changes and updating the repository.
 */
class RemovedAppReceiver : WearableListenerService(), KoinComponent {

    private val repository: WatchAppRepository by inject()
    private val iconRepository: WatchAppIconRepository by inject()

    override fun onMessageReceived(message: MessageEvent) {
        if (message.path == RemovedAppsList) {
            val packages = RemovedAppsSerializer.deserialize(message.data)
            val watchId = message.sourceNodeId

            runBlocking {
                repository.delete(watchId, packages.packages)

                packages.packages.forEach {
                    iconRepository.removeIconFor(watchId, it)
                }
            }
        }
    }
}
