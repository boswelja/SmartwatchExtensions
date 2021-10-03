package com.boswelja.smartwatchextensions.common.batterysync

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager

object References {
    const val BATTERY_STATUS_PATH = "/battery_status"
    const val REQUEST_BATTERY_UPDATE_PATH = "/request_battery_update"
}

/**
 * Get an up to date [BatteryStats] for this device.
 * @return The [BatteryStats] for this device, or null if there was an issue.
 */
fun Context.batteryStats(): BatteryStats? {
    val iFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    registerReceiver(null, iFilter)?.let {
        val batteryLevel = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val batteryScale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val percent = (batteryLevel * 100) / batteryScale
        val status = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val charging = (status == BatteryManager.BATTERY_STATUS_CHARGING) ||
            (status == BatteryManager.BATTERY_STATUS_FULL)
        return BatteryStats(percent, charging, System.currentTimeMillis())
    }
    return null
}
