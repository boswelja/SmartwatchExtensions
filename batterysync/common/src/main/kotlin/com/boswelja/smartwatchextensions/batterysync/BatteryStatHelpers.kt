package com.boswelja.smartwatchextensions.batterysync

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager

private const val BatteryPercentMultiplier = 100

/**
 * Get an up to date [BatteryStats] for this device.
 * @return The [BatteryStats] for this device, or null if there was an issue.
 */
fun Context.batteryStats(): BatteryStats? {
    // Get battery info
    val batteryInfo: Intent = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
        registerReceiver(null, ifilter)
    } ?: return null

    // Determine whether we're charging
    val status: Int = batteryInfo.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
    val isCharging: Boolean = status == BatteryManager.BATTERY_STATUS_CHARGING
            || status == BatteryManager.BATTERY_STATUS_FULL

    // Get battery percent
    val batteryPct: Float = batteryInfo.let { intent ->
        val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        level * BatteryPercentMultiplier / scale.toFloat()
    }

    // Create and return BatteryStats
    return BatteryStats(
        percent = batteryPct.toInt(),
        charging = isCharging,
        timestamp = System.currentTimeMillis()
    )
}
