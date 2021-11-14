package com.boswelja.smartwatchextensions.appmanager

import com.boswelja.watchconnection.serialization.MessageSerializer

/**
 * A [MessageSerializer] to handle serialization for a cache hash.
 */
object CacheValidationSerializer : MessageSerializer<Int> {

    private const val BYTE_MASK = 0xff

    private const val FIRST_BYTE_INDEX = 0
    private const val FIRST_BYTE_SHIFT = 24
    private const val SECOND_BYTE_INDEX = 1
    private const val SECOND_BYTE_SHIFT = 16
    private const val THIRD_BYTE_INDEX = 2
    private const val THIRD_BYTE_SHIFT = 8
    private const val FOURTH_BYTE_INDEX = 3
    private const val FOURTH_BYTE_SHIFT = 0

    override val messagePaths: Set<String> = setOf(VALIDATE_CACHE)

    override suspend fun deserialize(bytes: ByteArray?): Int {
        require(bytes!!.size == Int.SIZE_BYTES) { "Invalid Int received" }

        return BYTE_MASK and bytes[FIRST_BYTE_INDEX].toInt() shl FIRST_BYTE_SHIFT or
            (BYTE_MASK and bytes[SECOND_BYTE_INDEX].toInt() shl SECOND_BYTE_SHIFT) or
            (BYTE_MASK and bytes[THIRD_BYTE_INDEX].toInt() shl THIRD_BYTE_SHIFT) or
            (BYTE_MASK and bytes[FOURTH_BYTE_INDEX].toInt() shl FOURTH_BYTE_SHIFT)
    }

    override suspend fun serialize(data: Int): ByteArray {
        return byteArrayOf(
            (data ushr FIRST_BYTE_SHIFT and BYTE_MASK).toByte(),
            (data ushr SECOND_BYTE_SHIFT and BYTE_MASK).toByte(),
            (data ushr THIRD_BYTE_SHIFT and BYTE_MASK).toByte(),
            (data ushr FOURTH_BYTE_SHIFT and BYTE_MASK).toByte()
        )
    }
}
