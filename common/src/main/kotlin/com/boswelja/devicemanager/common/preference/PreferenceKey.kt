package com.boswelja.devicemanager.common.preference

object PreferenceKey {

    const val PHONE_LOCKING_ENABLED_KEY = "lock_phone_enabled"

    const val BATTERY_SYNC_ENABLED_KEY = "battery_sync_enabled"
    const val BATTERY_SYNC_INTERVAL_KEY = "battery_sync_interval"
    const val BATTERY_PHONE_CHARGE_NOTI_KEY = "battery_phone_charge_noti"
    const val BATTERY_WATCH_CHARGE_NOTI_KEY = "battery_watch_charge_noti"
    const val BATTERY_CHARGE_THRESHOLD_KEY = "battery_charge_threshold"
    const val BATTERY_PHONE_LOW_NOTI_KEY = "battery_phone_low_noti"
    const val BATTERY_WATCH_LOW_NOTI_KEY = "battery_watch_low_noti"
    const val BATTERY_LOW_THRESHOLD_KEY = "battery_low_threshold"

    const val BATTERY_CHARGED_NOTI_SENT = "battery_charged_notified"
    const val BATTERY_PERCENT_KEY = "battery_percent"

    const val DND_SYNC_TO_WATCH_KEY = "interrupt_filter_sync_to_watch"
    const val DND_SYNC_TO_PHONE_KEY = "interrupt_filter_sync_to_phone"
    const val DND_SYNC_WITH_THEATER_KEY = "interrupt_filter_sync_with_theater"
}
