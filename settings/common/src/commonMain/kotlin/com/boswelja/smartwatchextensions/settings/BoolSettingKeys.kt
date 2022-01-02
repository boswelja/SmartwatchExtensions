package com.boswelja.smartwatchextensions.settings

/**
 * Contains all shared Boolean settings keys.
 */
object BoolSettingKeys {

    /**
     * Whether phone locking is enabled.
     */
    const val PHONE_LOCKING_ENABLED_KEY = "lock_phone_enabled"

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
     * Whether the charge notification has been sent.
     */
    @Deprecated("")
    const val BATTERY_CHARGED_NOTI_SENT = "battery_charged_notified"

    /**
     * Whether the low notification has been sent.
     */
    @Deprecated("")
    const val BATTERY_LOW_NOTI_SENT = "battery_low_notified"

    /**
     * Whether the user has been notified about a Battery Stats event.
     */
    const val BATTERY_STATS_NOTIFICATION_SENT = "battery_stats_notified"

    /**
     * Whether DnD Sync to Watch is enabled.
     */
    const val DND_SYNC_TO_WATCH_KEY = "interrupt_filter_sync_to_watch"

    /**
     * Whether DnD Sync to Phone is enabled.
     */
    const val DND_SYNC_TO_PHONE_KEY = "interrupt_filter_sync_to_phone"

    /**
     * Whether DnD Sync with Theater is enabled.
     */
    const val DND_SYNC_WITH_THEATER_KEY = "interrupt_filter_sync_with_theater"

    /**
     * Whether watch separation notifications are enabled.
     */
    const val WATCH_SEPARATION_NOTI_KEY = "watch_separation_notification"

    /**
     * Whether phone separation notifications are enabled.
     */
    const val PHONE_SEPARATION_NOTI_KEY = "phone_separation_notification"
}
