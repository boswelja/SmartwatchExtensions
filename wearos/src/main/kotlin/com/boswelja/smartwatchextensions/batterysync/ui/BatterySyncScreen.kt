package com.boswelja.smartwatchextensions.batterysync.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.BatteryIcon
import com.boswelja.smartwatchextensions.common.ExtensionCard

@Composable
fun BatterySyncScreen(
    batterySyncEnabled: Boolean,
    batteryPercent: Int,
    phoneName: String,
    onClick: () -> Unit
) {
    BatteryStatus(
        percent = batteryPercent,
        phoneName = phoneName,
        onClick = onClick,
        enabled = batterySyncEnabled
    )
}

@Composable
fun BatteryStatus(
    percent: Int,
    phoneName: String,
    onClick: () -> Unit,
    enabled: Boolean
) {
    ExtensionCard(
        modifier = Modifier.fillMaxWidth(),
        icon = {
            if (enabled) {
                BatteryIcon(percent, modifier = Modifier.size(56.dp))
            } else {
                Icon(
                    painterResource(R.drawable.battery_unknown),
                    contentDescription = null
                )
            }
        },
        hintText = {
            if (enabled) {
                Text(
                    stringResource(R.string.battery_sync_hint_text, phoneName),
                    style = MaterialTheme.typography.body1,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    stringResource(R.string.battery_sync_disabled),
                    style = MaterialTheme.typography.body1,
                    textAlign = TextAlign.Center
                )
            }
        },
        content = {
            if (enabled) {
                Text(
                    stringResource(R.string.battery_percent, percent.toString()),
                    style = MaterialTheme.typography.display3,
                    textAlign = TextAlign.Center
                )
            }
        },
        onClick = onClick,
        enabled = enabled
    )
}
