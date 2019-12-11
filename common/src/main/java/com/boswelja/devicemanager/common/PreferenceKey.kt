/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.common

object PreferenceKey {

    const val PHONE_LOCKING_ENABLED_KEY = "lock_phone_enabled"

    const val BATTERY_SYNC_LAST_WHEN_KEY = "battery_sync_last_when"
    const val BATTERY_SYNC_ENABLED_KEY = "battery_sync_enabled"
    const val BATTERY_SYNC_INTERVAL_KEY = "battery_sync_interval"
    const val BATTERY_PHONE_CHARGE_NOTI_KEY = "battery_phone_charge_noti"
    const val BATTERY_WATCH_CHARGE_NOTI_KEY = "battery_watch_charge_noti"
    const val BATTERY_CHARGE_THRESHOLD_KEY = "battery_charge_threshold"

    const val BATTERY_CHARGED_NOTI_SENT = "battery_charged_notified"
    const val BATTERY_PERCENT_KEY = "battery_percent"

    const val DND_SYNC_TO_WATCH_KEY = "interrupt_filter_sync_to_watch"
    const val DND_SYNC_TO_PHONE_KEY = "interrupt_filter_sync_to_phone"
    const val DND_SYNC_WITH_THEATER_KEY = "interrupt_filter_sync_with_theater"
}
