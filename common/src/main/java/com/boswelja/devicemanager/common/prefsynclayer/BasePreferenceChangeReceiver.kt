package com.boswelja.devicemanager.common.prefsynclayer

import android.content.Intent
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.DnDLocalChangeListener
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.common.R
import com.boswelja.devicemanager.common.prefsynclayer.PreferenceSyncKeys.BATTERY_PHONE_FULL_CHARGE_NOTI_KEY
import com.boswelja.devicemanager.common.prefsynclayer.PreferenceSyncKeys.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.devicemanager.common.prefsynclayer.PreferenceSyncKeys.DND_SYNC_PHONE_TO_WATCH_KEY
import com.boswelja.devicemanager.common.prefsynclayer.PreferenceSyncKeys.DND_SYNC_WATCH_TO_PHONE_KEY
import com.boswelja.devicemanager.common.prefsynclayer.PreferenceSyncKeys.DND_SYNC_WITH_THEATER_KEY
import com.boswelja.devicemanager.common.prefsynclayer.PreferenceSyncKeys.LOCK_PHONE_ENABLED_KEY
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

abstract class BasePreferenceChangeReceiver : WearableListenerService() {

    lateinit var prefs: SharedPreferences

    override fun onDataChanged(dataEvents: DataEventBuffer?) {
        super.onDataChanged(dataEvents)
        dataEvents?.forEach { event ->
            val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap

            val dndSyncPhoneToWatch = dataMap.getBoolean(DND_SYNC_PHONE_TO_WATCH_KEY)
            val dndSyncWatchToPhone = dataMap.getBoolean(DND_SYNC_WATCH_TO_PHONE_KEY)
            val dndSyncWithTheater = dataMap.getBoolean(DND_SYNC_WITH_THEATER_KEY)

            val phoneBatteryChargedNoti = dataMap.getBoolean(BATTERY_PHONE_FULL_CHARGE_NOTI_KEY)
            val batterySyncEnabled = dataMap.getBoolean(BATTERY_SYNC_ENABLED_KEY)

            val lockPhoneEnabled = dataMap.getBoolean(LOCK_PHONE_ENABLED_KEY)

            prefs = PreferenceManager.getDefaultSharedPreferences(this)
            prefs.edit()
                    .putBoolean(PreferenceKey.DND_SYNC_PHONE_TO_WATCH_KEY, dndSyncPhoneToWatch)
                    .putBoolean(PreferenceKey.DND_SYNC_WATCH_TO_PHONE_KEY, dndSyncWatchToPhone)
                    .putBoolean(PreferenceKey.DND_SYNC_WITH_THEATER_MODE_KEY, dndSyncWithTheater)
                    .putBoolean(PreferenceKey.BATTERY_FULL_CHARGE_NOTI_KEY, phoneBatteryChargedNoti)
                    .putBoolean(PreferenceKey.LOCK_PHONE_ENABLED, lockPhoneEnabled)
                    .putBoolean(PreferenceKey.BATTERY_SYNC_ENABLED_KEY, batterySyncEnabled)
                    .apply()

            val isPhone = resources.getBoolean(R.bool.device_is_phone)
            if ((isPhone && dndSyncPhoneToWatch) || (!isPhone && dndSyncWatchToPhone)) {
                startLocalDnDListenerService()
            }

            handleStartServices(dndSyncWithTheater, batterySyncEnabled)
        }
    }

    private fun startLocalDnDListenerService() {
        val intent = Intent(applicationContext, DnDLocalChangeListener::class.java)
        Compat.startService(applicationContext, intent)
    }

    abstract fun handleStartServices(dndSyncWithTheater: Boolean, batterySyncEnabled: Boolean)

}