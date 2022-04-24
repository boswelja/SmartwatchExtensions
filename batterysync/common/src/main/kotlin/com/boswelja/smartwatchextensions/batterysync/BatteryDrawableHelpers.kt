package com.boswelja.smartwatchextensions.batterysync

import com.boswelja.smartwatchextensions.batterysync.common.R

private const val BatteryLevelAlert = 20
private const val BatteryLevel20 = 30
private const val BatteryLevel30 = 40
private const val BatteryLevel40 = 50
private const val BatteryLevel50 = 60
private const val BatteryLevel60 = 70
private const val BatteryLevel70 = 80
private const val BatteryLevel80 = 90
private const val BatteryLevel90 = 99
private const val BatteryLevelFull = 100

/**
 * Get the battery drawable resource corresponding to the given percentage.
 * @param percent The battery percent to get an icon for.
 */
fun getBatteryDrawableRes(percent: Int): Int {
    return when (percent) {
        in 0 until BatteryLevelAlert -> R.drawable.battery_alert
        in BatteryLevelAlert until BatteryLevel20 -> R.drawable.battery_20
        in BatteryLevel20 until BatteryLevel30 -> R.drawable.battery_30
        in BatteryLevel30 until BatteryLevel40 -> R.drawable.battery_40
        in BatteryLevel40 until BatteryLevel50 -> R.drawable.battery_50
        in BatteryLevel50 until BatteryLevel60 -> R.drawable.battery_60
        in BatteryLevel60 until BatteryLevel70 -> R.drawable.battery_70
        in BatteryLevel70 until BatteryLevel80 -> R.drawable.battery_80
        in BatteryLevel80 until BatteryLevel90 -> R.drawable.battery_90
        BatteryLevelFull -> R.drawable.battery_full
        else -> R.drawable.battery_unknown
    }
}
