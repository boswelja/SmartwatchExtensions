/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.common

object References {

    const val DND_SYNC_NOTI_CHANNEL_ID = "dnd_sync"
    const val BATTERY_CHARGED_NOTI_CHANEL_ID = "companion_device_charged"

    // Data map paths
    const val BATTERY_STATUS_PATH = "/batteryStatus"
    const val PREFERENCE_CHANGE_PATH = "/preferenceChange"

    const val BATTERY_PERCENT_KEY = "/batteryPercent"
    const val DND_STATUS_KEY = "/dndStatus"

    // Shared Preferences paths
    const val NEW_DND_STATE_PATH = "com.boswelja.devicemanager.dndenabled"
    const val DND_SYNC_SEND_PATH = "com.boswelja.devicemanager.dndsyncsend"
    const val DND_SYNC_RECEIVE_PATH = "com.boswelja.devicemanager.dndsyncreceive"
    const val LOCK_PHONE_ENABLED_PATH = "com.boswelja.devicemanager.lockphoneenabled"
    const val BATTERY_PERCENT_PATH = "com.boswelja.devicemanager.batterypercent"
    const val BATTERY_PHONE_FULL_CHARGE_NOTI_PATH = "com.boswelja.devicemanager.phonebattfullchargenotienabled"
    const val BATTERY_SYNC_ENABLED_PATH = "com.boswelja.devicemanager.batterysync"

    // Message keys
    const val LOCK_PHONE_KEY = "/lock_phone"
    const val REQUEST_BATTERY_UPDATE_KEY = "/request_battery_update"
    const val REQUEST_DND_ACCESS_STATUS = "/request_dnd_access_status"

    const val BATTERY_PERCENT_JOB_ID = 5656299
    const val NOTIFICATION_ID = 29137

    const val TYPE_LOCK_PHONE: Int = 1
    const val TYPE_PHONE_BATTERY: Int = 2

    const val CAPABILITY_APP = "extensions_app"

    const val INTENT_ACTION_EXTRA = "action"
}