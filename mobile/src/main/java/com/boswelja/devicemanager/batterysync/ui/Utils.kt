/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.batterysync.ui

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.boswelja.devicemanager.common.batterysync.References.BATTERY_STATUS_PATH
import com.google.android.gms.wearable.Wearable
import timber.log.Timber

object Utils {

    fun updateBatteryStats(context: Context, target: String?) {
        if (!target.isNullOrEmpty()) {
            Timber.i("Updating battery stats")
            val iFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus = context.registerReceiver(null, iFilter)
            val batteryPct =
                (
                    (
                        batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)!! /
                            batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1).toFloat()
                        ) * 100
                    )
                    .toInt()
            val charging =
                batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ==
                    BatteryManager.BATTERY_STATUS_CHARGING
            val message = "$batteryPct|$charging"

            Wearable.getMessageClient(context)
                .sendMessage(target, BATTERY_STATUS_PATH, message.toByteArray(Charsets.UTF_8))
        } else {
            Timber.w("target null or empty, can't update battery stats")
        }
    }
}
