package com.boswelja.devicemanager.common.prefsynclayer

import android.content.Context
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.common.prefsynclayer.PreferenceSyncKeys.BATTERY_PHONE_FULL_CHARGE_NOTI_KEY
import com.boswelja.devicemanager.common.prefsynclayer.PreferenceSyncKeys.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.devicemanager.common.prefsynclayer.PreferenceSyncKeys.BATTERY_WATCH_FULL_CHARGE_NOTI_KEY
import com.boswelja.devicemanager.common.prefsynclayer.PreferenceSyncKeys.DND_SYNC_PHONE_TO_WATCH_KEY
import com.boswelja.devicemanager.common.prefsynclayer.PreferenceSyncKeys.DND_SYNC_WATCH_TO_PHONE_KEY
import com.boswelja.devicemanager.common.prefsynclayer.PreferenceSyncKeys.DND_SYNC_WITH_THEATER_KEY
import com.boswelja.devicemanager.common.prefsynclayer.PreferenceSyncKeys.LOCK_PHONE_ENABLED_KEY
import com.boswelja.devicemanager.common.prefsynclayer.PreferenceSyncKeys.PREFERENCE_CHANGE_PATH
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable

class PreferenceSyncLayer(context: Context) {

    private val localPrefs = PreferenceManager.getDefaultSharedPreferences(context)
    private val dataClient = Wearable.getDataClient(context)

    fun pushNewData() {
        // Get updated prefs
        val batterySyncEnabled = localPrefs.getBoolean(PreferenceKey.BATTERY_SYNC_ENABLED_KEY, false)
        val phoneBatteryChargedNoti = localPrefs.getBoolean(PreferenceKey.BATTERY_PHONE_FULL_CHARGE_NOTI_KEY, false)
        val watchBatteryChargedNoti = localPrefs.getBoolean(PreferenceKey.BATTERY_WATCH_FULL_CHARGE_NOTI_KEY, false)
        val dndSyncPhoneToWatch = localPrefs.getBoolean(PreferenceKey.DND_SYNC_PHONE_TO_WATCH_KEY, false)
        val dndSyncWatchToPhone = localPrefs.getBoolean(PreferenceKey.DND_SYNC_WATCH_TO_PHONE_KEY, false)
        val dndSyncWithTheater = localPrefs.getBoolean(PreferenceKey.DND_SYNC_WITH_THEATER_MODE_KEY, false)
        val lockPhoneEnabled = localPrefs.getBoolean(PreferenceKey.LOCK_PHONE_ENABLED, false)

        // Create updated prefs object
        val syncedPrefUpdateReq = PutDataMapRequest.create(PREFERENCE_CHANGE_PATH)
        syncedPrefUpdateReq.dataMap.putBoolean(BATTERY_SYNC_ENABLED_KEY, batterySyncEnabled)
        syncedPrefUpdateReq.dataMap.putBoolean(BATTERY_PHONE_FULL_CHARGE_NOTI_KEY, phoneBatteryChargedNoti)
        syncedPrefUpdateReq.dataMap.putBoolean(BATTERY_WATCH_FULL_CHARGE_NOTI_KEY, watchBatteryChargedNoti)
        syncedPrefUpdateReq.dataMap.putBoolean(DND_SYNC_PHONE_TO_WATCH_KEY, dndSyncPhoneToWatch)
        syncedPrefUpdateReq.dataMap.putBoolean(DND_SYNC_WATCH_TO_PHONE_KEY, dndSyncWatchToPhone)
        syncedPrefUpdateReq.dataMap.putBoolean(DND_SYNC_WITH_THEATER_KEY, dndSyncWithTheater)
        syncedPrefUpdateReq.dataMap.putBoolean(LOCK_PHONE_ENABLED_KEY, lockPhoneEnabled)

        // Send updated prefs
        syncedPrefUpdateReq.setUrgent()
        dataClient.putDataItem(syncedPrefUpdateReq.asPutDataRequest())
    }
}