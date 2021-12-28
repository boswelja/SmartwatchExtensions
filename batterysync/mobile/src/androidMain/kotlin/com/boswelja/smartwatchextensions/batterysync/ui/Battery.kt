package com.boswelja.smartwatchextensions.batterysync.ui

import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.boswelja.smartwatchextensions.batterysync.common.getBatteryDrawableRes

/**
 * Displays a battery icon for the given percentage.
 * @param percent The battery percent to display an icon for.
 * @param modifier [Modifier].
 */
@Composable
fun BatteryIcon(
    percent: Int,
    modifier: Modifier = Modifier
) {
    val drawable = getBatteryDrawableRes(percent)
    Icon(
        painterResource(drawable),
        null,
        modifier
    )
}
