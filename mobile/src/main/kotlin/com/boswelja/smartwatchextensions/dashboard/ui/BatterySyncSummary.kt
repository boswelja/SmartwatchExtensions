package com.boswelja.smartwatchextensions.dashboard.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.batterysync.BatteryStats
import com.boswelja.smartwatchextensions.core.ui.toMinutes

/**
 * A Composable for displaying a summary of a [BatteryStats].
 */
@Suppress("MagicNumber")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatterySyncSummary(
    batteryStats: BatteryStats,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier
) {
    val resources = LocalContext.current.resources
    val animatedBattery by animateFloatAsState(
        targetValue = batteryStats.percent / 100f,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
    )
    val ageMinutes by batteryStats.timestamp.toMinutes()

    Card(
        onClick = onClick,
        modifier = modifier
    ) {
        Column(contentModifier) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.battery_sync_title),
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = if (ageMinutes > 0) {
                        resources.getQuantityString(R.plurals.data_age_short, ageMinutes, ageMinutes)
                    } else {
                        stringResource(R.string.data_age_recent_short)
                    },
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Spacer(Modifier.height(2.dp))

            Text(
                text = stringResource(
                    com.boswelja.smartwatchextensions.batterysync.R.string.battery_percent,
                    batteryStats.percent.toString()
                ),
                style = MaterialTheme.typography.titleMedium
            )
            LinearProgressIndicator(
                progress = animatedBattery,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * A Composable for displaying a summary of a [BatteryStats].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatterySyncLoadingSummary(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
    ) {
        Column(contentModifier) {
            Text(
                text = stringResource(R.string.battery_sync_title),
                style = MaterialTheme.typography.labelMedium
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = stringResource(
                    com.boswelja.smartwatchextensions.batterysync.R.string.battery_sync_header_loading
                ),
                style = MaterialTheme.typography.titleMedium
            )
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
