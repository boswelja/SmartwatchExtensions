package com.boswelja.smartwatchextensions.dashboard.ui

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.boswelja.smartwatchextensions.batterysync.BatteryStats
import com.boswelja.smartwatchextensions.batterysync.R
import com.boswelja.smartwatchextensions.batterysync.getBatteryDrawableRes

private const val ICON_WIDTH_PERCENT = 0.33f

/**
 * A Composable for displaying a small summary of a [BatteryStats].
 * @param modifier [Modifier].
 * @param batteryStats The battery stats to show info for.
 */
@Composable
fun BatterySummarySmall(
    modifier: Modifier = Modifier,
    batteryStats: BatteryStats
) {
    FeatureSummarySmall(
        modifier = modifier,
        icon = {
            BatteryIcon(
                percent = batteryStats.percent,
                modifier = Modifier
                    .fillMaxWidth(ICON_WIDTH_PERCENT)
                    .aspectRatio(1f)
            )
        },
        text = {
            Text(
                text = stringResource(
                    R.string.battery_percent,
                    batteryStats.percent.toString()
                ),
                style = MaterialTheme.typography.titleLarge
            )
        }
    )
}

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
