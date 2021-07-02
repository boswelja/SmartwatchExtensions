package com.boswelja.smartwatchextensions.common

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.wear.compose.material.Icon

@Composable
fun BatteryIcon(
    percent: Int,
    modifier: Modifier = Modifier
) {
    val drawable = getBatteryResource(percent)
    Icon(
        painterResource(drawable),
        null,
        modifier,
        tint = Color.White
    )
}

@DrawableRes
fun getBatteryResource(percent: Int): Int {
    return when (percent) {
        in 0..20 -> R.drawable.battery_alert
        in 20..30 -> R.drawable.battery_20
        in 30..40 -> R.drawable.battery_30
        in 40..50 -> R.drawable.battery_40
        in 50..60 -> R.drawable.battery_50
        in 60..70 -> R.drawable.battery_60
        in 70..80 -> R.drawable.battery_70
        in 80..90 -> R.drawable.battery_80
        in 90..99 -> R.drawable.battery_90
        100 -> R.drawable.battery_full
        else -> R.drawable.battery_unknown
    }
}
