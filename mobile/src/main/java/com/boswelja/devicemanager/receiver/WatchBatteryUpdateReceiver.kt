/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.receiver

import com.boswelja.devicemanager.common.BatteryUpdateListener
import com.boswelja.devicemanager.widget.WatchBatteryWidget

class WatchBatteryUpdateReceiver : BatteryUpdateListener() {

    override fun onBatteryUpdate(percent: Int, charging: Boolean) {
        WatchBatteryWidget.updateWidget(this)
    }
}