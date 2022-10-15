package com.boswelja.smartwatchextensions.wearableinterface.playservices

import android.content.Context
import com.boswelja.smartwatchextensions.wearableinterface.ChannelManager
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await
import java.io.InputStream
import java.io.OutputStream

internal class ChannelManagerImpl(context: Context) : ChannelManager {

    private val channelClient = Wearable.getChannelClient(context)

    override suspend fun receiveDataFrom(watchId: String, path: String, block: suspend (InputStream) -> Unit) {
        val channel = channelClient.openChannel(watchId, path).await()
        requireNotNull(channel) { "Failed to create channel $path for target with ID $watchId" }

        try {
            channelClient.getInputStream(channel).await().use { block(it) }
        } finally {
            channelClient.close(channel)
        }
    }

    override suspend fun sendDataTo(watchId: String, path: String, block: suspend (OutputStream) -> Unit) {
        val channel = channelClient.openChannel(watchId, path).await()
        requireNotNull(channel) { "Failed to create channel $path for target with ID $watchId" }

        try {
            channelClient.getOutputStream(channel).await().use { block(it) }
        } finally {
            channelClient.close(channel)
        }
    }
}
