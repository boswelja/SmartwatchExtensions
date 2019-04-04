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
import android.support.wearable.complications.ProviderUpdateRequester
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.common.prefsynclayer.BasePreferenceChangeReceiver
import com.boswelja.devicemanager.complications.PhoneBatteryComplicationProvider

class PreferenceChangeListener : BasePreferenceChangeReceiver() {

    override fun handleStartServices(dndSyncWithTheater: Boolean, batterySyncEnabled: Boolean) {
        if (!batterySyncEnabled) {
            prefs.edit().remove(PreferenceKey.BATTERY_PERCENT_KEY).apply()
            ProviderUpdateRequester(this, ComponentName(packageName, PhoneBatteryComplicationProvider::class.java.name)).requestUpdateAll()
        }
        if (dndSyncWithTheater) {
            Compat.startService(this, Intent(applicationContext, DnDSyncWithTheaterModeListener::class.java))
        }
    }

}