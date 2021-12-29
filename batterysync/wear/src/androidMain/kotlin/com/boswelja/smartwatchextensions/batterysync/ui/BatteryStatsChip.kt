package com.boswelja.smartwatchextensions.batterysync.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import com.boswelja.smartwatchextensions.batterysync.R

/**
 * A [Chip] for showing Battery Stats info.
 * @param modifier [Modifier].
 * @param percent The paired phone battery percent.
 * @param phoneName The paired phone name.
 * @param enabled Whether Battery Sync is enabled.
 * @param onClick Called when the chip is clicked.
 */
@Composable
fun BatteryStatsChip(
    modifier: Modifier = Modifier,
    percent: Int,
    phoneName: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val colors = if (enabled)
        ChipDefaults.gradientBackgroundChipColors()
    else
        ChipDefaults.secondaryChipColors()

    Chip(
        modifier = modifier,
        icon = {
            if (enabled) {
                BatteryIcon(percent)
            } else {
                Icon(painterResource(R.drawable.battery_unknown), null)
            }
        },
        label = {
            if (enabled) {
                Text(stringResource(R.string.battery_percent, percent.toString()))
            } else {
                Text(stringResource(R.string.battery_sync_disabled))
            }
        },
        secondaryLabel = if (enabled) {
            { Text(stringResource(R.string.battery_sync_hint_text, phoneName)) }
        } else null,
        onClick = onClick,
        enabled = enabled,
        colors = colors
    )
}
