package com.boswelja.smartwatchextensions.phonelocking

import kotlinx.serialization.Serializable

@Serializable
data class PhoneLockingState(
    val phoneLockingEnabled: Boolean
)
