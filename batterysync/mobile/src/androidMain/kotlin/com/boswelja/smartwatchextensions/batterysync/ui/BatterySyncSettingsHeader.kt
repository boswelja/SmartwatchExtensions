package com.boswelja.smartwatchextensions.batterysync.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProgressIndicatorDefaults
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.boswelja.smartwatchextensions.batterysync.BatteryStats
import com.boswelja.smartwatchextensions.batterysync.R
import com.boswelja.smartwatchextensions.batterysync.ui.BatterySliderDefaults.PROGRESS_FACTOR
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.minutes

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
 * Return a State that produces the elapsed minutes since this timestamp.
 */
@Composable
internal fun Long.toMinutes() = produceState(
    initialValue = TimeUnit.MINUTES.convert(System.currentTimeMillis() - this, TimeUnit.MILLISECONDS),
    key1 = this
) {
    while (true) {
        value = TimeUnit.MINUTES.convert(System.currentTimeMillis() - this@toMinutes, TimeUnit.MILLISECONDS)
        delay(1.minutes)
    }
}
