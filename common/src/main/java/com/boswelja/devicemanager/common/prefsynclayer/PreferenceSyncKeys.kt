package com.boswelja.devicemanager.common.prefsynclayer

object PreferenceSyncKeys {

    const val PREFERENCE_CHANGE_PATH = "/preference_change"

    private const val packageName = "com.boswelja.devicemanager"

    const val BATTERY_SYNC_ENABLED_KEY = "$packageName.batterysync"
    const val BATTERY_PHONE_FULL_CHARGE_NOTI_KEY = "$packageName.phonebattfullchargenotienabled"
    const val BATTERY_WATCH_FULL_CHARGE_NOTI_KEY = "$packageName.watchbattfullchargenotienabled"

    const val DND_SYNC_PHONE_TO_WATCH_KEY = "$packageName.dndsyncphonetowatch"
    const val DND_SYNC_WATCH_TO_PHONE_KEY = "$packageName.dndsyncwatchtophone"
    const val DND_SYNC_WITH_THEATER_KEY = "$packageName.dndsyncwiththeater"

    const val LOCK_PHONE_ENABLED_KEY = "$packageName.lockphoneenabled"

}