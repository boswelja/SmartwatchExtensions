package com.boswelja.devicemanager.common

object Extensions {

    /**
     * Convert a single Boolean to a ByteArray of length 1
     */
    fun Boolean.toByteArray() : ByteArray {
        val byte: Byte = if (this) {
            1
        } else {
            0
        }
        return byteArrayOf(byte)
    }

    /**
     * Convert a length 1 ByteArray to a Boolean
     */
    fun Boolean.Companion.fromByteArray(byteArray: ByteArray) : Boolean {
        return byteArray[0].toInt() == 1
    }
}