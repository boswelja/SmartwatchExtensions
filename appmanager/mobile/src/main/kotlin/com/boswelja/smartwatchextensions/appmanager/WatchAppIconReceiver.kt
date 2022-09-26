package com.boswelja.smartwatchextensions.appmanager

import android.content.Context
import com.boswelja.watchconnection.common.message.MessageReceiver
import com.boswelja.watchconnection.common.message.ReceivedMessage
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * A [MessageReceiver] for receiving [AppIcon] and updating the repository
 */
class WatchAppIconReceiver : MessageReceiver(), KoinComponent {

    private val watchAppIconRepository: WatchAppIconRepository by inject()

    override suspend fun onMessageReceived(context: Context, message: ReceivedMessage<ByteArray?>) {
        if (message.path == RawAppIcon) {
            val iconData = message.data?.let { AppIconSerializer.deserialize(it) } ?: return
            watchAppIconRepository.storeIconFor(
                message.sourceUid,
                iconData.packageName,
                iconData.iconBytes
            )
        }
    }
}
