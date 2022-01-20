package com.boswelja.smartwatchextensions.batterysync

import com.boswelja.smartwatchextensions.batterysync.common.R

private const val BATTERY_LEVEL_ALERT = 20
private const val BATTERY_LEVEL_20 = 30
private const val BATTERY_LEVEL_30 = 40
private const val BATTERY_LEVEL_40 = 50
private const val BATTERY_LEVEL_50 = 60
private const val BATTERY_LEVEL_60 = 70
private const val BATTERY_LEVEL_70 = 80
private const val BATTERY_LEVEL_80 = 90
private const val BATTERY_LEVEL_90 = 99
private const val BATTERY_LEVEL_FULL = 100

/**
 * Get the battery drawable resource corresponding to the given percentage.
 * @param percent The battery percent to get an icon for.
 */
fun getBatteryDrawableRes(percent: Int): Int {
    return when (percent) {
        in 0 until BATTERY_LEVEL_ALERT -> R.drawable.battery_alert
        in BATTERY_LEVEL_ALERT until BATTERY_LEVEL_20 -> R.drawable.battery_20
        in BATTERY_LEVEL_20 until BATTERY_LEVEL_30 -> R.drawable.battery_30
        in BATTERY_LEVEL_30 until BATTERY_LEVEL_40 -> R.drawable.battery_40
        in BATTERY_LEVEL_40 until BATTERY_LEVEL_50 -> R.drawable.battery_50
        in BATTERY_LEVEL_50 until BATTERY_LEVEL_60 -> R.drawable.battery_60
        in BATTERY_LEVEL_60 until BATTERY_LEVEL_70 -> R.drawable.battery_70
        in BATTERY_LEVEL_70 until BATTERY_LEVEL_80 -> R.drawable.battery_80
        in BATTERY_LEVEL_80 until BATTERY_LEVEL_90 -> R.drawable.battery_90
        BATTERY_LEVEL_FULL -> R.drawable.battery_full
        else -> R.drawable.battery_unknown
    }
}
