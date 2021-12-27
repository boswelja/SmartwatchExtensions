package com.boswelja.smartwatchextensions.batterysync

import kotlinx.serialization.Serializable

/**
 * Contains information about a device battery status.
 * @param percent The device battery percent.
 * @param charging Whether the device is currently charging.
 * @param timestamp The timestamp of these stats, measured in milliseconds since UNIX epoch.
 */
@Serializable
data class BatteryStats(
    val percent: Int,
    val charging: Boolean,
    val timestamp: Long
)
