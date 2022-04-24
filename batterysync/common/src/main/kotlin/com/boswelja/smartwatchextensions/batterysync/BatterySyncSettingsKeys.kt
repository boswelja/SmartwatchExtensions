package com.boswelja.smartwatchextensions.batterysync

/**
 * Contains all shared Int settings keys.
 */
object BatterySyncSettingsKeys {

    /**
     * Whether Battery Sync is enabled.
     */
    const val BATTERY_SYNC_ENABLED_KEY = "battery_sync_enabled"

    /**
     * Whether phone charge notifications are enabled.
     */
    const val BATTERY_PHONE_CHARGE_NOTI_KEY = "battery_phone_charge_noti"

    /**
     * Whether watch charge notifications are enabled.
     */
    const val BATTERY_WATCH_CHARGE_NOTI_KEY = "battery_watch_charge_noti"

    /**
     * Whether phone low notifications are enabled.
     */
    const val BATTERY_PHONE_LOW_NOTI_KEY = "battery_phone_low_noti"

    /**
     * Whether watch low notifications are enabled.
     */
    const val BATTERY_WATCH_LOW_NOTI_KEY = "battery_watch_low_noti"

    /**
     * Whether the user has been notified about a Battery Stats event.
     */
    const val BATTERY_STATS_NOTIFICATION_SENT = "battery_stats_notified"

    /**
     * The battery threshold to consider the device charged at.
     */
    const val BATTERY_CHARGE_THRESHOLD_KEY = "battery_charge_threshold"

    /**
     * The battery threshold to consider the device low at.
     */
    const val BATTERY_LOW_THRESHOLD_KEY = "battery_low_threshold"
}
