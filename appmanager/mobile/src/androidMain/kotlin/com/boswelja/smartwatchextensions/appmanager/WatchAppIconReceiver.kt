package com.boswelja.smartwatchextensions.appmanager

import android.content.Context
import com.boswelja.watchconnection.common.message.ReceivedMessage
import com.boswelja.watchconnection.serialization.MessageReceiver
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * A [MessageReceiver] for receiving [AppIcon] and updating the repository
 */
class WatchAppIconReceiver : MessageReceiver<AppIcon>(AppIconSerializer), KoinComponent {

    private val watchAppIconRepository: WatchAppIconRepository by inject()

    override suspend fun onMessageReceived(context: Context, message: ReceivedMessage<AppIcon>) {
        watchAppIconRepository.storeIconFor(
            message.sourceUid,
            message.data.packageName,
            message.data.iconBytes
        )
    }
}
