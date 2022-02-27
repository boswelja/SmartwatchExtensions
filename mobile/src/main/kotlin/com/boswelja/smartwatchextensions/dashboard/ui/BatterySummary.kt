package com.boswelja.smartwatchextensions.dashboard.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.boswelja.smartwatchextensions.batterysync.BatteryStats
import com.boswelja.smartwatchextensions.batterysync.R
import com.boswelja.smartwatchextensions.core.ui.toMinutes

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
    val resources = LocalContext.current.resources
    val animatedBattery by animateFloatAsState(
        targetValue = batteryStats.percent / 100f,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
    )
    val ageMinutes by batteryStats.timestamp.toMinutes()
    Column(modifier) {
        Text(
            text = stringResource(
                R.string.battery_percent,
                batteryStats.percent.toString()
            ),
            style = MaterialTheme.typography.titleLarge
        )
        LinearProgressIndicator(
            progress = animatedBattery
        )
        Text(
            text = if (ageMinutes > 0) {
                resources.getQuantityString(com.boswelja.smartwatchextensions.R.plurals.data_age_short, ageMinutes, ageMinutes)
            } else {
                stringResource(com.boswelja.smartwatchextensions.R.string.data_age_recent_short)
            }
        )
    }
}
