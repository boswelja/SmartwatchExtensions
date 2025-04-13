package com.boswelja.smartwatchextensions.appmanager

import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * A [WearableListenerService] for receiving [AppList] changes and updating the repository.
 */
class AddedOrUpdatedAppReceiver : WearableListenerService(), KoinComponent {

    private val repository: WatchAppRepository by inject()

    override fun onMessageReceived(message: MessageEvent) {
        if (message.path == AddedAppsList || message.path == UpdatedAppsList) {
            val appList = AddedOrUpdatedAppsSerializer.deserialize(message.data)
            val apps = appList.mapToWatchAppDetails(message.sourceNodeId)
            runBlocking { repository.updateAll(apps) }
        }
    }
}
