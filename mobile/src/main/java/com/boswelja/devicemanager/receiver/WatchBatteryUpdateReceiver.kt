/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.receiver

import com.boswelja.devicemanager.common.batterysync.BatteryUpdateReceiver
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.widget.WatchBatteryWidget

class WatchBatteryUpdateReceiver : BatteryUpdateReceiver() {

    override fun sendChargeNotiEnabled(): Boolean =
            sharedPreferences.getBoolean(PreferenceKey.BATTERY_PHONE_FULL_CHARGE_NOTI_KEY, false)

    override fun onBatteryUpdate(percent: Int, charging: Boolean) {
        WatchBatteryWidget.updateWidgets(this)
    }
}
