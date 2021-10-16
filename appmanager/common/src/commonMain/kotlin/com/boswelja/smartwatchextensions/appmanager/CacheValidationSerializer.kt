package com.boswelja.smartwatchextensions.appmanager

import com.boswelja.watchconnection.common.message.MessageSerializer

object CacheValidationSerializer : MessageSerializer<Int>(
    messagePaths = setOf(VALIDATE_CACHE)
) {
    override suspend fun deserialize(bytes: ByteArray): Int {
        require(bytes.size == 4) { "Invalid Int received" }

        return (0xff and bytes[0].toInt() shl 24) or
            (0xff and bytes[1].toInt() shl 16) or
            (0xff and bytes[2].toInt() shl 8) or
            (0xff and bytes[3].toInt() shl 0)
    }

    override suspend fun serialize(data: Int): ByteArray {
        return byteArrayOf(
            (data ushr 24 and 0xff).toByte(),
            (data ushr 16 and 0xff).toByte(),
            (data ushr 8 and 0xff).toByte(),
            (data ushr 0 and 0xff).toByte()
        )
    }
}
