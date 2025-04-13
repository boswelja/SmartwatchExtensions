package com.boswelja.smartwatchextensions.watchmanager.data

import com.boswelja.smartwatchextensions.common.RequestAppVersion
import com.boswelja.smartwatchextensions.common.Version
import com.boswelja.smartwatchextensions.common.VersionSerializer
import com.boswelja.smartwatchextensions.watchmanager.domain.WatchVersionRepository
import com.boswelja.smartwatchextensions.wearable.ext.receiveMessages
import com.boswelja.smartwatchextensions.wearable.ext.sendMessage
import com.google.android.gms.wearable.MessageClient
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds

class WatchVersionRepositoryImpl(
    private val messageClient: MessageClient
) : WatchVersionRepository {
    override suspend fun getWatchVersion(watchId: String): Version {
        // Send the request
        check(messageClient.sendMessage(targetId = watchId, RequestAppVersion, null)) {
            "Failed to send $RequestAppVersion to $watchId!"
        }

        // Wait up to 5 seconds for a response
        return withTimeout(5.seconds) {
            val message = messageClient.receiveMessages()
                .filter { it.sourceNodeId == watchId && it.path == RequestAppVersion }
                .first()
            VersionSerializer.deserialize(message.data)
        }
    }
}
