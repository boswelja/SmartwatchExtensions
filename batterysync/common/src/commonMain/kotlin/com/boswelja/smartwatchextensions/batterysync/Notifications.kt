package com.boswelja.smartwatchextensions.batterysync

/**
 * Check whether a battery charged notification should be posted for this [BatteryStats].
 * @param threshold The batteru charge threshold to check against. [BatteryStats.percent] must be
 * greater than or equal to this.
 * @param notificationsEnabled Whether the user has enabled battery charge notifications.
 * @param hasNotifiedAlready Whether a notification has already been posted for the source device.
 * @return true if a notification should be posted, false otherwise
 */
fun BatteryStats.shouldPostChargeNotification(
    threshold: Int,
    notificationsEnabled: Boolean,
    hasNotifiedAlready: Boolean
): Boolean {
    return notificationsEnabled && !hasNotifiedAlready && percent >= threshold
}

/**
 * Check whether a battery low notification should be posted for this [BatteryStats].
 * @param threshold The battery low threshold to check against. [BatteryStats.percent] must be
 * less than or equal to this.
 * @param notificationsEnabled Whether the user has enabled battery low notifications.
 * @param hasNotifiedAlready Whether a notification has already been posted for the source device.
 * @return true if a notification should be posted, false otherwise
 */
fun BatteryStats.shouldPostLowNotification(
    threshold: Int,
    notificationsEnabled: Boolean,
    hasNotifiedAlready: Boolean
): Boolean {
    return notificationsEnabled && !hasNotifiedAlready && percent <= threshold
}
