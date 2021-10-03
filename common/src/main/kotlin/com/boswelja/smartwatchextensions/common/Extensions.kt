@file:Suppress("DeprecatedCallableAddReplaceWith")

package com.boswelja.smartwatchextensions.common

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle

/**
 * Convert a single [Boolean] to a [ByteArray].
 * @return The created [ByteArray].
 */
@Deprecated("Avoid sending raw ByteArray")
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
@Deprecated("Avoid sending raw ByteArray")
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
@Deprecated("Avoid sending raw ByteArray")
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
@Deprecated("Avoid sending raw ByteArray")
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

inline fun <reified T> Context.startActivity(
    flags: Int = 0,
    extras: Bundle? = null,
    options: ActivityOptions? = null
) {
    val intent = Intent(this, T::class.java).apply {
        extras?.let { putExtras(extras) }
        this.flags = flags
    }
    startActivity(intent, options?.toBundle())
}

inline fun Context.startActivity(
    options: Bundle? = null,
    intentBuilder: (Intent) -> Intent
) {
    val intent = Intent().let(intentBuilder)
    startActivity(intent, options)
}
