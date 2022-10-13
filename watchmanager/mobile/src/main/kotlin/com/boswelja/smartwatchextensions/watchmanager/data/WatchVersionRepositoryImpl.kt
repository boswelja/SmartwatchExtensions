package com.boswelja.smartwatchextensions.watchmanager.data

import com.boswelja.smartwatchextensions.common.RequestAppVersion
import com.boswelja.smartwatchextensions.common.Version
import com.boswelja.smartwatchextensions.common.VersionSerializer
import com.boswelja.smartwatchextensions.watchmanager.domain.WatchVersionRepository
import com.boswelja.watchconnection.common.message.Message
import com.boswelja.watchconnection.common.message.MessageClient
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds

class WatchVersionRepositoryImpl(
    private val messageClient: MessageClient
) : WatchVersionRepository {
    override suspend fun getWatchVersion(watchId: String): Version {
        // Send the request
        check(messageClient.sendMessage(watchId, Message(RequestAppVersion, null))) {
            "Failed to send $RequestAppVersion to $watchId!"
        }

        // Wait up to 5 seconds for a response
        return withTimeout(5.seconds) {
            val message = messageClient.incomingMessages()
                .filter { it.sourceUid == watchId && it.path == RequestAppVersion }
                .first()
            VersionSerializer.deserialize(message.data!!)
        }
    }
}
