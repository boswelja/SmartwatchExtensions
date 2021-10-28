package com.boswelja.smartwatchextensions.common

import com.boswelja.watchconnection.common.message.MessageSerializer

/**
 * A [MessageSerializer] for handling messages with no data.
 */
class EmptySerializer(messagePaths: Set<String>) : MessageSerializer<Nothing?>(messagePaths) {
    override suspend fun deserialize(bytes: ByteArray): Nothing? {
        return null
    }

    override suspend fun serialize(data: Nothing?): ByteArray {
        return byteArrayOf()
    }
}
