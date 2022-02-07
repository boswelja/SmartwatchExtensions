package com.boswelja.smartwatchextensions.core.settings

import kotlinx.serialization.Serializable

/**
 * Contains a boolean setting.
 * @param key The setting key.
 * @param value the boolean value.
 */
@Serializable
data class BoolSetting(
    val key: String,
    val value: Boolean
)

/**
 * Contains a int setting.
 * @param key The setting key.
 * @param value the int value.
 */
@Serializable
data class IntSetting(
    val key: String,
    val value: Int
)
