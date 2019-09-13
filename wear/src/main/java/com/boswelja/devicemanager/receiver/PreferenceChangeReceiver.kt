/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.receiver

import android.content.ComponentName
import android.content.Intent
import android.support.wearable.complications.ProviderUpdateRequester
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.common.prefsynclayer.BasePreferenceChangeReceiver
import com.boswelja.devicemanager.complication.PhoneBatteryComplicationProvider
import com.boswelja.devicemanager.service.InterruptFilterLocalChangeListener
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.Wearable

class PreferenceChangeReceiver : BasePreferenceChangeReceiver() {

    override fun onPreferenceChangeReceived(preferenceChangeEvents: DataEventBuffer?) {
        if (preferenceChangeEvents != null) {
            val currentNodeId = Tasks.await(Wearable.getNodeClient(this).localNode).id
            for (event in preferenceChangeEvents) {
                if (event.dataItem.uri.toString().endsWith(currentNodeId)) {
                    handlePreferenceChange(event)
                }
            }
        }
    }

    override fun onPreferenceChanged(key: String, newValue: Any) {
        when (key) {
            PreferenceKey.BATTERY_SYNC_ENABLED_KEY -> {
                if (newValue == false) {
                    prefs.edit().remove(PreferenceKey.BATTERY_PERCENT_KEY).apply()
                    ProviderUpdateRequester(this, ComponentName(packageName, PhoneBatteryComplicationProvider::class.java.name)).requestUpdateAll()
                }
            }
            PreferenceKey.INTERRUPT_FILTER_SYNC_TO_PHONE_KEY,
            PreferenceKey.INTERRUPT_FILTER_ON_WITH_THEATER_KEY -> {
                if (newValue == true) {
                    Intent(this, InterruptFilterLocalChangeListener::class.java).also {
                        Compat.startForegroundService(this, it)
                    }
                }
            }
        }
    }
}
