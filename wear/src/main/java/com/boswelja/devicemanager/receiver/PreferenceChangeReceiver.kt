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
import com.boswelja.devicemanager.service.InterruptFilterSyncWithTheaterListener

class PreferenceChangeReceiver : BasePreferenceChangeReceiver() {

    override fun handleStartServices(
        interruptFilterSyncToPhone: Boolean,
        interruptFilterSyncToWatch: Boolean,
        interruptFilterSyncWithTheater: Boolean,
        batterySyncEnabled: Boolean
    ) {
        if (interruptFilterSyncToPhone) {
            val intent = Intent(this, InterruptFilterLocalChangeListener::class.java)
            Compat.startForegroundService(this, intent)
        }

        if (!batterySyncEnabled) {
            prefs.edit().remove(PreferenceKey.BATTERY_PERCENT_KEY).apply()
            ProviderUpdateRequester(this, ComponentName(packageName, PhoneBatteryComplicationProvider::class.java.name)).requestUpdateAll()
        }

        if (interruptFilterSyncWithTheater) {
            Compat.startForegroundService(this, Intent(applicationContext, InterruptFilterSyncWithTheaterListener::class.java))
        }
    }
}
