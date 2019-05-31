/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.receiver

import android.content.ComponentName
import android.support.wearable.complications.ProviderUpdateRequester
import com.boswelja.devicemanager.common.batterysync.BatteryUpdateReceiver
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.common.References
import com.boswelja.devicemanager.common.batterysync.Utils.updateBatteryStats
import com.boswelja.devicemanager.complication.PhoneBatteryComplicationProvider

class PhoneBatteryUpdateReceiver : BatteryUpdateReceiver() {

    override fun sendChargeNotiEnabled(): Boolean =
            sharedPreferences.getBoolean(PreferenceKey.BATTERY_WATCH_FULL_CHARGE_NOTI_KEY, false)

    override fun onBatteryUpdate(percent: Int, charging: Boolean) {
        updateBatteryStats(this, References.CAPABILITY_PHONE_APP)
        ProviderUpdateRequester(this, ComponentName(packageName, PhoneBatteryComplicationProvider::class.java.name)).requestUpdateAll()
    }
}
