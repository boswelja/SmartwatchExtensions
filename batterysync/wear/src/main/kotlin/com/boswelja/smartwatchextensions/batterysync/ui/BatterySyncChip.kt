package com.boswelja.smartwatchextensions.batterysync.ui

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Text
import androidx.wear.widget.ConfirmationOverlay
import com.boswelja.smartwatchextensions.batterysync.R
import com.boswelja.smartwatchextensions.core.fold
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

/**
 * A [Chip] for showing Battery Sync info.
 * @param phoneName The paired phone name.
 * @param modifier [Modifier].
 */
@Composable
fun BatterySyncChip(
    phoneName: String,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()
    val viewModel: BatteryStatsViewModel = koinViewModel()
    val batteryStatsFeatureData by viewModel.batteryStats.collectAsState()

    batteryStatsFeatureData.fold(
        success = { batteryStats ->
            BatterySyncDetailsChip(
                percent = batteryStats.percent,
                phoneName = phoneName,
                onClick = {
                    coroutineScope.launch {
                        val result = viewModel.trySyncBattery()
                        if (result) {
                            view.showConfirmationOverlay(
                                type = ConfirmationOverlay.SUCCESS_ANIMATION,
                                message = view.context.getString(R.string.battery_sync_refresh_success)
                            )
                        } else {
                            view.showConfirmationOverlay(
                                type = ConfirmationOverlay.FAILURE_ANIMATION,
                                message = view.context.getString(R.string.phone_not_connected)
                            )
                        }
                    }
                },
                modifier = modifier
            )
        },
        disabled = {
            BatterySyncDisabledChip(modifier = modifier)
        },
        error = {
            // TODO
        },
        loading = {
            BatterySyncDisabledChip(modifier = modifier)
        }
    )
}

/**
 * Displays battery stats in a Chip. This should only be used when Battery Sync is enabled.
 * @param percent The paired phone battery percent.
 * @param phoneName The paired phone name.
 * @param onClick Called when the chip is clicked.
 * @param modifier [Modifier].
 */
@Composable
fun BatterySyncDetailsChip(
    percent: Int,
    phoneName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Chip(
        icon = { BatteryIcon(percent) },
        label = { Text(stringResource(R.string.battery_percent, percent.toString())) },
        secondaryLabel = { Text(stringResource(R.string.battery_sync_hint_text, phoneName)) },
        onClick = onClick,
        colors = ChipDefaults.gradientBackgroundChipColors(),
        modifier = modifier
    )
}

/**
 * Displays a Battery Sync Disabled Chip.
 * @param modifier [Modifier].
 */
@Composable
fun BatterySyncDisabledChip(
    modifier: Modifier = Modifier
) {
    Chip(
        icon = { BatteryIcon(-1) },
        label = { Text(stringResource(R.string.battery_sync_disabled)) },
        colors = ChipDefaults.secondaryChipColors(),
        onClick = {
            // Do nothing
        },
        enabled = false,
        modifier = modifier
    )
}

private fun View.showConfirmationOverlay(
    type: Int,
    message: CharSequence,
    duration: Int = ConfirmationOverlay.DEFAULT_ANIMATION_DURATION_MS
) {
    ConfirmationOverlay()
        .setDuration(duration)
        .setType(type)
        .setMessage(message)
        .showAbove(rootView)
}
