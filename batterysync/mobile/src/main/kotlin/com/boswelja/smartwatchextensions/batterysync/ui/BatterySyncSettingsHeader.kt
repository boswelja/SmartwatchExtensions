package com.boswelja.smartwatchextensions.batterysync.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.boswelja.smartwatchextensions.batterysync.BatteryStats
import com.boswelja.smartwatchextensions.batterysync.R
import com.boswelja.smartwatchextensions.batterysync.ui.BatterySliderDefaults.PROGRESS_FACTOR
import com.boswelja.smartwatchextensions.core.ui.toMinutes

/**
 * Display a header for Battery Sync settings.
 */
@Composable
fun BatterySyncSettingsHeader(
    batterySyncEnabled: Boolean,
    batteryStats: BatteryStats?,
    modifier: Modifier
) {
    val resources = LocalContext.current.resources

    BatteryStatsSummaryLarge(
        text = {
            val text = if (batterySyncEnabled) {
                batteryStats?.percent?.let {
                    stringResource(R.string.battery_percent, it)
                } ?: stringResource(R.string.battery_sync_header_disabled)
            } else {
                stringResource(R.string.battery_sync_header_disabled)
            }
            Text(text)
        },
        secondaryText = {
            val text = if (batterySyncEnabled) {
                batteryStats?.let {
                    val dataAgeMinutes by it.timestamp.toMinutes()
                    if (dataAgeMinutes > 0) {
                        resources.getQuantityString(
                            R.plurals.battery_sync_last_updated_minutes,
                            dataAgeMinutes,
                            dataAgeMinutes
                        )
                    } else {
                        stringResource(R.string.battery_sync_last_update_recent)
                    }
                } ?: stringResource(R.string.battery_sync_header_loading)
            } else {
                stringResource(R.string.battery_sync_disabled)
            }
            Text(text)
        },
        batteryProgress = if (batterySyncEnabled) (batteryStats?.percent ?: 0) / PROGRESS_FACTOR else 0f,
        modifier = modifier
    )
}

/**
 * A Composable for displaying a large summary of a [BatteryStats].
 * @param modifier [Modifier].
 */
@Composable
fun BatteryStatsSummaryLarge(
    text: @Composable () -> Unit,
    secondaryText: @Composable () -> Unit,
    batteryProgress: Float,
    modifier: Modifier = Modifier
) {
    val animatedBattery by animateFloatAsState(
        targetValue = batteryProgress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
    )
    Column(modifier) {
        ProvideTextStyle(MaterialTheme.typography.headlineMedium) {
            text()
        }
        LinearProgressIndicator(
            progress = { animatedBattery },
            modifier = Modifier.fillMaxWidth(),
        )
        ProvideTextStyle(MaterialTheme.typography.bodyLarge) {
            secondaryText()
        }
    }
}
