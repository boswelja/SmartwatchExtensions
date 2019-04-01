/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.service

import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.support.wearable.complications.ProviderUpdateRequester
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.DnDLocalChangeListener
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.common.References
import com.boswelja.devicemanager.complications.PhoneBatteryComplicationProvider
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

class PreferenceChangeListener : WearableListenerService() {

    override fun onDataChanged(dataEvents: DataEventBuffer?) {
        super.onDataChanged(dataEvents)
        dataEvents?.forEach { event ->
            val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap

            // Inverted because when phone is sending, watch is receiving
            val dndReceiving = dataMap.getBoolean(References.DND_SYNC_SEND_KEY)
            val dndSending = dataMap.getBoolean(References.DND_SYNC_RECEIVE_KEY)
            val dndSyncWithTheater = dataMap.getBoolean(References.DND_SYNC_WITH_THEATER_KEY)

            val phoneBatteryChargedNoti = dataMap.getBoolean(References.BATTERY_PHONE_FULL_CHARGE_NOTI_KEY)
            val batterySyncEnabled = dataMap.getBoolean(References.BATTERY_SYNC_ENABLED_KEY)

            val lockPhoneEnabled = dataMap.getBoolean(References.LOCK_PHONE_ENABLED_KEY)

            val prefs = PreferenceManager.getDefaultSharedPreferences(this)
            prefs.edit()
                    .putBoolean(PreferenceKey.DND_SYNC_SEND_KEY, dndSending)
                    .putBoolean(PreferenceKey.DND_SYNC_RECEIVE_KEY, dndReceiving)
                    .putBoolean(PreferenceKey.DND_SYNC_WITH_THEATER_MODE_KEY, dndSyncWithTheater)
                    .putBoolean(PreferenceKey.BATTERY_FULL_CHARGE_NOTI_KEY, phoneBatteryChargedNoti)
                    .putBoolean(PreferenceKey.LOCK_PHONE_ENABLED, lockPhoneEnabled)
                    .putBoolean(PreferenceKey.BATTERY_SYNC_ENABLED_KEY, batterySyncEnabled)
                    .apply()

            if (dndSending) {
                val intent = Intent(applicationContext, DnDLocalChangeListener::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent)
                } else {
                    startService(intent)
                }
            }

            if (dndSyncWithTheater) {
                Compat.startService(this, Intent(applicationContext, DnDSyncWithTheaterModeListener::class.java))
            }

            if (!batterySyncEnabled) {
                prefs.edit().remove(PreferenceKey.BATTERY_PERCENT_KEY).apply()
                ProviderUpdateRequester(this, ComponentName(packageName, PhoneBatteryComplicationProvider::class.java.name)).requestUpdateAll()
            }
        }
    }
}