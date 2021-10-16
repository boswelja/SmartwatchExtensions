package com.boswelja.smartwatchextensions.common.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.wear.compose.material.Icon
import com.boswelja.smartwatchextensions.batterysync.common.getBatteryDrawableRes

@Composable
fun BatteryIcon(
    percent: Int,
    modifier: Modifier = Modifier
) {
    val drawable = getBatteryDrawableRes(percent)
    Icon(
        painterResource(drawable),
        null,
        modifier,
        tint = Color.White
    )
}
