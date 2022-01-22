package com.boswelja.smartwatchextensions.phonelocking

import kotlinx.serialization.Serializable

/**
 * Contains values to track the state of the Phone Locking feature.
 * @param phoneLockingEnabled Whether phone locking is enabled.
 */
@Serializable
data class PhoneLockingState(
    val phoneLockingEnabled: Boolean
)
