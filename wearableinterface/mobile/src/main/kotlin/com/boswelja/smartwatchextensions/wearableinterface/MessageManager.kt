package com.boswelja.smartwatchextensions.wearableinterface

import kotlinx.coroutines.flow.Flow
import java.io.IOException

interface MessageManager {
    @Throws(IOException::class)
    suspend fun sendMessage(
        watchId: String,
        path: String,
        data: ByteArray? = null,
        priority: MessagePriority = MessagePriority.Low
    )

    fun receiveMessages(): Flow<ReceivedMessage>
}

enum class MessagePriority {
    High,
    Low
}

data class ReceivedMessage(
    val sourceWatchId: String,
    val path: String,
    val data: ByteArray?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ReceivedMessage

        if (sourceWatchId != other.sourceWatchId) return false
        if (path != other.path) return false
        if (data != null) {
            if (other.data == null) return false
            if (!data.contentEquals(other.data)) return false
        } else if (other.data != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sourceWatchId.hashCode()
        result = 31 * result + path.hashCode()
        result = 31 * result + (data?.contentHashCode() ?: 0)
        return result
    }
}