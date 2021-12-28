package com.boswelja.smartwatchextensions.batterysync.ui

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.batterysync.BatteryStats
import com.boswelja.smartwatchextensions.common.ui.FeatureSummarySmall

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
                text = stringResource(R.string.battery_percent, batteryStats.percent.toString()),
                style = MaterialTheme.typography.h5
            )
        }
    )
}
