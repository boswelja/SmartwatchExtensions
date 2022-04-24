package com.boswelja.smartwatchextensions.batterysync

import android.content.Context
import com.boswelja.batterystats.BatteryInfo
import com.boswelja.batterystats.BatteryManager

private const val BatteryPercentMultiplier = 100

/**
 * Get an up to date [BatteryStats] for this device.
 * @return The [BatteryStats] for this device, or null if there was an issue.
 */
fun Context.batteryStats(): BatteryStats? {
    return BatteryManager(this).getBatteryInfo()?.let {
        BatteryStats(
            (it.percent * BatteryPercentMultiplier).toInt(),
            it.status == BatteryInfo.Status.CHARGING,
            System.currentTimeMillis()
        )
    }
}