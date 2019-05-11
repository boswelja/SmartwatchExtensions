/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.common.prefsynclayer

object PreferenceSyncKeys {

    const val PREFERENCE_CHANGE_PATH = "/preference_change"

    private const val packageName = "com.boswelja.devicemanager"

    const val BATTERY_SYNC_ENABLED_KEY = "$packageName.battery-sync"
    const val BATTERY_PHONE_FULL_CHARGE_NOTI_KEY = "$packageName.phone-charge-noti-enabled"
    const val BATTERY_WATCH_FULL_CHARGE_NOTI_KEY = "$packageName.watch-charge-noti-enabled"

    const val DND_SYNC_PHONE_TO_WATCH_KEY = "$packageName.interrupt-filter-sync-to-watch"
    const val DND_SYNC_WATCH_TO_PHONE_KEY = "$packageName.interrupt-filter-sync-to-phone"
    const val DND_SYNC_WITH_THEATER_KEY = "$packageName.interrupt-filter-sync-with-theater"

    const val LOCK_PHONE_ENABLED_KEY = "$packageName.lock-phone-enabled"
}
