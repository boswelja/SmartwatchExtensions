package com.boswelja.smartwatchextensions.dndsync

/**
 * A serializer for handling DnD status.
 */
object DnDStatusSerializer {
    private const val trueByte: Byte = 1
    private const val falseByte: Byte = 0

    fun deserialize(bytes: ByteArray?): Boolean {
        require(bytes!!.size == 1) { "Invalid data received" }
        return bytes.first() == trueByte
    }

    fun serialize(data: Boolean): ByteArray = byteArrayOf(if (data) trueByte else falseByte)
}
