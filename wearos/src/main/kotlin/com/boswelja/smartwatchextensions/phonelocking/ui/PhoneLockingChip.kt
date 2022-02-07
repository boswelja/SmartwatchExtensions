package com.boswelja.smartwatchextensions.phonelocking.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhonelinkLock
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text

/**
 * A [Chip] for displaying Phone Locking info.
 * @param modifier [Modifier].
 * @param phoneName The paired phone's name.
 * @param enabled Whether phone locking is enabled.
 * @param onClick Called when the chip is clicked.
 */
@Composable
fun PhoneLockingChip(
    modifier: Modifier = Modifier,
    phoneName: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val colors = if (enabled)
        ChipDefaults.gradientBackgroundChipColors()
    else
        ChipDefaults.secondaryChipColors()

    Chip(
        modifier = modifier,
        icon = {
            Icon(Icons.Outlined.PhonelinkLock, null)
        },
        label = {
            if (enabled) {
                Text(stringResource(com.boswelja.smartwatchextensions.phonelocking.R.string.lock_phone, phoneName))
            } else {
                Text(
                    stringResource(
                        com.boswelja.smartwatchextensions.phonelocking.R.string.lock_phone_disabled,
                        phoneName
                    )
                )
            }
        },
        onClick = onClick,
        colors = colors,
        enabled = enabled
    )
}
