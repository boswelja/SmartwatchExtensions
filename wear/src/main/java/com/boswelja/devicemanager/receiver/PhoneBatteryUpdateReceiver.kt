/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.receiver

import android.content.ComponentName
import android.support.wearable.complications.ProviderUpdateRequester
import com.boswelja.devicemanager.common.BatteryUpdateReceiver
import com.boswelja.devicemanager.common.CommonUtils
import com.boswelja.devicemanager.complication.PhoneBatteryComplicationProvider

class PhoneBatteryUpdateReceiver : BatteryUpdateReceiver() {

    override fun onBatteryUpdate(percent: Int, charging: Boolean) {
        CommonUtils.updateBatteryStats(this)
        ProviderUpdateRequester(this, ComponentName(packageName, PhoneBatteryComplicationProvider::class.java.name)).requestUpdateAll()
    }
}