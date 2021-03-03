package com.boswelja.devicemanager.common.preference

object SyncPreferences {

    val INT_PREFS = listOf(PreferenceKey.BATTERY_CHARGE_THRESHOLD_KEY)
    val BOOL_PREFS =
        listOf(
            PreferenceKey.PHONE_LOCKING_ENABLED_KEY,
            PreferenceKey.BATTERY_SYNC_ENABLED_KEY,
            PreferenceKey.BATTERY_PHONE_CHARGE_NOTI_KEY,
            PreferenceKey.BATTERY_WATCH_CHARGE_NOTI_KEY,
            PreferenceKey.DND_SYNC_TO_PHONE_KEY,
            PreferenceKey.DND_SYNC_TO_WATCH_KEY,
            PreferenceKey.DND_SYNC_WITH_THEATER_KEY
        )
    val ALL_PREFS = INT_PREFS + BOOL_PREFS
}
