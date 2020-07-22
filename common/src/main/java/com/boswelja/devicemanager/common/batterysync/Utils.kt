/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.common.batterysync

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.boswelja.devicemanager.common.batterysync.References.BATTERY_STATUS_PATH
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable

object Utils {

    /**
     * Sends a battery status update to connected devices.
     */
    fun updateBatteryStats(context: Context, target: String) {
        val iFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, iFilter)
        val batteryPct = ((batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)!! / batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1).toFloat()) * 100).toInt()
        val charging = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1) == BatteryManager.BATTERY_STATUS_CHARGING
        val message = "$batteryPct|$charging"

        Wearable.getCapabilityClient(context)
            .getCapability(target, CapabilityClient.FILTER_REACHABLE)
            .addOnSuccessListener { capabilityInfo ->
                for (node in capabilityInfo.nodes) {
                    val messageClient = Wearable.getMessageClient(context)
                    messageClient.sendMessage(
                        node.id,
                        BATTERY_STATUS_PATH,
                        message.toByteArray(Charsets.UTF_8)
                    )
                }
            }
    }
}
