/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.common

object References {

    const val DND_SYNC_NOTIFICATION_CHANNEL_ID = "dnd_sync"

    // Data map paths
    const val BATTERY_STATUS_PATH = "/batteryStatus"
    const val PREFERENCE_CHANGE_PATH = "/preferenceChange"

    const val BATTERY_PERCENT_KEY = "/batteryPercent"
    const val DND_STATUS_KEY = "/dndStatus"

    // Shared Preferences paths
    const val NEW_DND_STATE_PATH = "com.boswelja.devicemanager.dndenabled"
    const val NEW_DND_STATE_CHANGED_BY_PATH = "com.boswelja.devicemanager.dndchangedby"
    const val DND_SYNC_ENABLED_PATH = "com.boswelja.devicemanager.dndsyncenabled"
    const val DND_SYNC_SEND_PATH = "com.boswelja.devicemanager.dndsyncsend"
    const val DND_SYNC_RECEIVE_PATH = "com.boswelja.devicemanager.dndsyncreceive"
    const val LOCK_PHONE_ENABLED_PATH = "com.boswelja.devicemanager.lockphoneenabled"
    const val BATTERY_PERCENT_PATH = "com.boswelja.devicemanager.batterypercent"
    const val BATTERY_CHARGING = "com.boswelja.devicemanager.batterycharging"
    const val BATTERY_PHONE_FULL_CHARGE_NOTI_PATH = "com.boswelja.devicemanager.phonebattfullchargenotienabled"

    // Message keys
    const val LOCK_PHONE_KEY = "/lock_phone"
    const val REQUEST_BATTERY_UPDATE_KEY = "/request_battery_update"

    const val BATTERY_PERCENT_JOB_ID = 5656299

    const val DEVICE_ADMIN_REQUEST_CODE = 665

    const val TYPE_LOCK_PHONE: Int = 1
    const val TYPE_PHONE_BATTERY: Int = 2

    const val CAPABILITY_PHONE_APP = "extensions_mobile_app"

    const val INTENT_ACTION_EXTRA = "action"

}