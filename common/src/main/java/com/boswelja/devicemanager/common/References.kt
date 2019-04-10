/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.common

object References {

    private const val packageName = "com.boswelja.devicemanager"

    // Notification channel IDs
    const val DND_SYNC_NOTI_CHANNEL_ID = "dnd_sync"
    const val BATTERY_CHARGED_NOTI_CHANEL_ID = "companion_device_charged"

    // DnD Sync
    const val DND_STATUS_KEY = "/dnd_status"
    const val NEW_DND_STATE_KEY = "$packageName.dndenabled"

    // Message paths
    const val LOCK_PHONE_PATH = "/lock_phone"
    const val REQUEST_LAUNCH_APP_PATH = "/launch_app"
    const val BATTERY_STATUS_PATH = "/battery_status"
    const val REQUEST_BATTERY_UPDATE_PATH = "/request_battery_update"
    const val REQUEST_PHONE_DND_ACCESS_STATUS_PATH = "/request_phone_dnd_access_status"
    const val REQUEST_WATCH_DND_ACCESS_STATUS_PATH = "/request_watch_dnd_access_status"

    const val CAPABILITY_APP = "extensions_app"
}