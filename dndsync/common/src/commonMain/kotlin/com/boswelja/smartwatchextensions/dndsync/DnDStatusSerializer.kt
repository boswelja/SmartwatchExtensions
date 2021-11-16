package com.boswelja.smartwatchextensions.dndsync

import com.boswelja.watchconnection.serialization.MessageSerializer

/**
 * A [MessageSerializer] for handling DnD status.
 */
object DnDStatusSerializer : MessageSerializer<Boolean> {
    private const val trueByte: Byte = 1
    private const val falseByte: Byte = 0

    override val messagePaths: Set<String> = setOf(DND_STATUS_PATH)

    override suspend fun deserialize(bytes: ByteArray?): Boolean {
        require(bytes!!.size == 1) { "Invalid data received" }
        return bytes.first() == trueByte
    }

    override suspend fun serialize(data: Boolean): ByteArray =
        byteArrayOf(if (data) trueByte else falseByte)
}
