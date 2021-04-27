package com.boswelja.smartwatchextensions.common.connection

object Preference {

    /**
     * Convert  a Preference [Pair] to a [ByteArray].
     */
    fun Pair<String, Any>.toByteArray(): ByteArray {
        return "$first|$second".toByteArray(Charsets.UTF_8)
    }

    /**
     * Get a preference [Pair] from a [ByteArray].
     */
    inline fun <reified T> fromByteArray(data: ByteArray): Pair<String, T> {
        val parts = String(data, Charsets.UTF_8).split("|")
        val key = parts[0]
        val value = when (T::class) {
            Int::class -> {
                parts[1].toInt()
            }
            Boolean::class -> {
                parts[1] == "true"
            }
            else -> throw IllegalArgumentException()
        } as T
        return Pair(key, value)
    }
}
