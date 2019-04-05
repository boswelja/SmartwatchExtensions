/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.receiver

import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.common.BatteryUpdateListener
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.widget.WatchBatteryWidget

class WatchBatteryUpdateListener : BatteryUpdateListener() {

    override fun onBatteryUpdate(percent: Int, charging: Boolean) {
        WatchBatteryWidget.updateWidget(this)
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putLong(PreferenceKey.BATTERY_SYNC_LAST_WHEN_KEY, System.currentTimeMillis())
                .apply()
    }
}