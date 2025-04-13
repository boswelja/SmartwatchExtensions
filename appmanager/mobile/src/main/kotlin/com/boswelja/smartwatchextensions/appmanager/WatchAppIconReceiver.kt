package com.boswelja.smartwatchextensions.appmanager

import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * A [WearableListenerService] for receiving [AppIcon] and updating the repository
 */
class WatchAppIconReceiver : WearableListenerService(), KoinComponent {

    private val watchAppIconRepository: WatchAppIconRepository by inject()

    override fun onMessageReceived(message: MessageEvent) {
        if (message.path == RawAppIcon) {
            val iconData = AppIconSerializer.deserialize(message.data)
            runBlocking {
                watchAppIconRepository.storeIconFor(
                    message.sourceNodeId,
                    iconData.packageName,
                    iconData.iconBytes
                )
            }
        }
    }
}
