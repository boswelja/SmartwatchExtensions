package com.boswelja.smartwatchextensions.batterysync.domain

import kotlinx.serialization.Serializable

/**
 * Contains values to track the state of the Battery Sync feature.
 * @param batterySyncEnabled Whether Battery Sync is enabled.
 * @param phoneChargeNotificationEnabled Whether phone charge notifications are enabled.
 * @param phoneLowNotificationEnabled Whether phone low notifications are enabled.
 * @param phoneChargeThreshold The minimum battery percent at which to consider the phone charged.
 * @param phoneLowThreshold The maximum percent at which to consider the phone low.
 * @param notificationPosted Whether phone charge/low notifications have been posted.
 */
@Serializable
data class BatterySyncState(
    val batterySyncEnabled: Boolean,
    val phoneChargeNotificationEnabled: Boolean,
    val phoneLowNotificationEnabled: Boolean,
    val phoneChargeThreshold: Int,
    val phoneLowThreshold: Int,
    val notificationPosted: Boolean
)
