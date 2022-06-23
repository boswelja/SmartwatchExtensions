package com.boswelja.smartwatchextensions.batterysync.domain.model

/**
 * Represents the state of battery notifications for a particular device.
 * @param chargeNotificationsEnabled Whether charge notifications are enabled for the device.
 * @param lowNotificationsEnabled Whether low notifications are enabled for the device.
 */
data class DeviceBatteryNotificationState(
    val chargeNotificationsEnabled: Boolean,
    val lowNotificationsEnabled: Boolean
)
