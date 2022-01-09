package com.boswelja.smartwatchextensions.batterysync

import com.boswelja.smartwatchextensions.batterysync.common.R

private const val BATTERY_ALERT_LEVEL = 20
private const val BATTERY_20_LEVEL = 30
private const val BATTERY_30_LEVEL = 40
private const val BATTERY_40_LEVEL = 50
private const val BATTERY_50_LEVEL = 60
private const val BATTERY_60_LEVEL = 70
private const val BATTERY_70_LEVEL = 80
private const val BATTERY_80_LEVEL = 90
private const val BATTERY_90_LEVEL = 99
private const val BATTERY_FULL_LEVEL = 100

/**
 * Get the battery drawable resource corresponding to the given percentage.
 * @param percent The battery percent to get an icon for.
 */
fun getBatteryDrawableRes(percent: Int): Int {
    return when (percent) {
        in 0 until BATTERY_ALERT_LEVEL -> R.drawable.battery_alert
        in BATTERY_ALERT_LEVEL until BATTERY_20_LEVEL -> R.drawable.battery_20
        in BATTERY_20_LEVEL until BATTERY_30_LEVEL -> R.drawable.battery_30
        in BATTERY_30_LEVEL until BATTERY_40_LEVEL -> R.drawable.battery_40
        in BATTERY_40_LEVEL until BATTERY_50_LEVEL -> R.drawable.battery_50
        in BATTERY_50_LEVEL until BATTERY_60_LEVEL -> R.drawable.battery_60
        in BATTERY_60_LEVEL until BATTERY_70_LEVEL -> R.drawable.battery_70
        in BATTERY_70_LEVEL until BATTERY_80_LEVEL -> R.drawable.battery_80
        in BATTERY_80_LEVEL until BATTERY_90_LEVEL -> R.drawable.battery_90
        BATTERY_FULL_LEVEL -> R.drawable.battery_full
        else -> R.drawable.battery_unknown
    }
}
