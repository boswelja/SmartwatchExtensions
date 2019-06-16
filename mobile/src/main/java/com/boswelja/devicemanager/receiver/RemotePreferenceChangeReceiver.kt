/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.receiver

import android.content.Intent
import com.boswelja.devicemanager.BatteryUpdateJob
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.common.prefsynclayer.BasePreferenceChangeReceiver
import com.boswelja.devicemanager.service.InterruptFilterLocalChangeListener

class RemotePreferenceChangeReceiver : BasePreferenceChangeReceiver() {

    override fun onPreferenceChanged(key: String, newValue: Any) {
        when (key) {
            PreferenceKey.BATTERY_SYNC_ENABLED_KEY -> {
                if (newValue == true) {
                    BatteryUpdateJob.startJob(this)
                } else {
                    BatteryUpdateJob.stopJob(this)
                }
            }
            PreferenceKey.INTERRUPT_FILTER_SYNC_TO_WATCH_KEY -> {
                if (newValue == true) {
                    Intent(this, InterruptFilterLocalChangeListener::class.java).also {
                        Compat.startForegroundService(this, it)
                    }
                }
            }
        }
    }
}
