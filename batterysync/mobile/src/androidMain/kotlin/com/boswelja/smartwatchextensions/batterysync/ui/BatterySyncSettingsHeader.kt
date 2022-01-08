package com.boswelja.smartwatchextensions.batterysync.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProgressIndicatorDefaults
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.smartwatchextensions.batterysync.BatteryStats
import com.boswelja.smartwatchextensions.batterysync.R
import com.boswelja.smartwatchextensions.batterysync.ui.BatterySliderDefaults.PROGRESS_FACTOR
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

/**
 * Display a header for Battery Sync settings.
 */
@Composable
fun BatterySyncSettingsHeader() {
    val resources = LocalContext.current.resources

    val viewModel: BatterySyncViewModel = viewModel()
    val batterySyncEnabled by viewModel.batterySyncEnabled.collectAsState()
    val batteryStats by viewModel.batteryStats.collectAsState()

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
                batteryStats?.timestamp?.let {
                    val dataAgeMinutes by timestampToAge(
                        timestamp = it,
                        ageUnit = TimeUnit.MINUTES
                    )
                    if (dataAgeMinutes > 0) {
                        resources.getQuantityString(
                            R.plurals.battery_sync_last_updated_minutes,
                            dataAgeMinutes.toInt(),
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
//
//    if (batterySyncEnabled) {
//        val batteryStats by viewModel.batteryStats.collectAsState()
//        batteryStats.let {
//            if (it != null) {
//                val dataAgeMinutes by timestampToAge(
//                    timestamp = it.timestamp,
//                    ageUnit = TimeUnit.MINUTES
//                )
//                val batteryProgress = remember(batteryStats) { it.percent / BatterySliderDefaults.PROGRESS_FACTOR }
//                BatteryStatsSummaryLarge(
//                    text = { Text(stringResource(R.string.battery_percent, it.percent.toString())) },
//                    secondaryText = {
//                        Text(
//                            if (dataAgeMinutes > 0) {
//                                resources.getQuantityString(
//                                    R.plurals.battery_sync_last_updated_minutes,
//                                    dataAgeMinutes.toInt(),
//                                    dataAgeMinutes
//                                )
//                            } else {
//                                stringResource(R.string.battery_sync_last_update_recent)
//                            }
//                        )
//                    },
//                    batteryProgress = batteryProgress,
//                    modifier = headerModifier
//                )
//            } else {
//                BatteryStatsSummaryLarge(
//                    text = { Text(stringResource(R.string.battery_sync_header_disabled)) },
//                    secondaryText = { Text(stringResource(R.string.battery_sync_header_loading)) },
//                    batteryProgress = 0f,
//                    modifier = headerModifier
//                )
//            }
//        }
//    } else {
//        BatteryStatsSummaryLarge(
//            text = { Text(stringResource(R.string.battery_sync_header_disabled)) },
//            secondaryText = { Text(stringResource(R.string.battery_sync_disabled)) },
//            batteryProgress = 0f,
//            modifier = headerModifier
//        )
//    }
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
        ProvideTextStyle(MaterialTheme.typography.h4) {
            text()
        }
        LinearProgressIndicator(
            progress = animatedBattery,
            modifier = Modifier.fillMaxWidth()
        )
        ProvideTextStyle(MaterialTheme.typography.body1) {
            secondaryText()
        }
    }
}

/**
 * Convert a timestamp to a human readable age value.
 * @param timestamp The timestamp in milliseconds to convert.
 * @param ageUnit The [TimeUnit] to display the age in.
 */
@Composable
fun timestampToAge(timestamp: Long, ageUnit: TimeUnit) = produceState(
    initialValue = ageUnit.convert(System.currentTimeMillis() - timestamp, TimeUnit.MILLISECONDS)
) {
    while (true) {
        delay(ageUnit.toMillis(1))
        value = ageUnit.convert(System.currentTimeMillis() - timestamp, TimeUnit.MILLISECONDS)
    }
}
