package com.boswelja.smartwatchextensions.batterysync.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Text
import com.boswelja.smartwatchextensions.batterysync.R

/**
 * A [Chip] for showing Battery Sync info.
 * @param percent The paired phone battery percent.
 * @param phoneName The paired phone name.
 * @param enabled Whether Battery Sync is enabled.
 * @param onClick Called when the chip is clicked.
 * @param modifier [Modifier].
 */
@Composable
fun BatterySyncChip(
    percent: Int,
    phoneName: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (enabled) {
        BatterySyncDetailsChip(
            percent = percent,
            phoneName = phoneName,
            onClick = onClick,
            modifier = modifier
        )
    } else {
        BatterySyncDisabledChip(modifier = modifier)
    }
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
