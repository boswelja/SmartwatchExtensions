/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.common

object Extensions {

    /**
     * Convert a single [Boolean] to a [ByteArray].
     * @return The created [ByteArray].
     */
    fun Boolean.toByteArray(): ByteArray {
        val byte: Byte =
            if (this) {
                1
            } else {
                0
            }
        return byteArrayOf(byte)
    }

    /**
     * Convert a [ByteArray] to a [Boolean], returns false if the given [ByteArray] is empty.
     * @param byteArray The [ByteArray] to convert to a [Boolean].
     * @return The [Boolean] result, or false if [ByteArray] was invalid.
     */
    fun Boolean.Companion.fromByteArray(byteArray: ByteArray): Boolean {
        return if (byteArray.isNotEmpty()) {
            byteArray[0].toInt() == 1
        } else {
            false
        }
    }

    /**
     * Convert a single [Int] to a [ByteArray].
     * @return The created [ByteArray].
     */
    fun Int.toByteArray(): ByteArray {
        return byteArrayOf(
            (this ushr 24 and 0xff).toByte(),
            (this ushr 16 and 0xff).toByte(),
            (this ushr 8 and 0xff).toByte(),
            (this ushr 0 and 0xff).toByte()
        )
    }

    /**
     * Convert a [ByteArray] to an [Int], returns false if the given [ByteArray] is empty.
     * @param byteArray The [ByteArray] to convert to an [Int].
     * @return The [Int] from the given [ByteArray]
     */
    fun Int.Companion.fromByteArray(byteArray: ByteArray): Int {
        return if (byteArray.size != 4) {
            -1
        } else {
            (0xff and byteArray[0].toInt() shl 24) or
                (0xff and byteArray[1].toInt() shl 16) or
                (0xff and byteArray[2].toInt() shl 8) or
                (0xff and byteArray[3].toInt() shl 0)
        }
    }
}
