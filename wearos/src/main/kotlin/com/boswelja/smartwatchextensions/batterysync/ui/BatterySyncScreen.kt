package com.boswelja.smartwatchextensions.batterysync.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.BatteryIcon

@Composable
fun BatterySyncScreen(
    batterySyncEnabled: Boolean,
    batteryPercent: Int,
    phoneName: String,
    onClick: () -> Unit
) {
    if (batterySyncEnabled) {
        BatteryStatus(
            percent = batteryPercent,
            phoneName = phoneName,
            onClick = onClick
        )
    } else {
        BatterySyncDisabled()
    }
}

@Composable
fun BatterySyncDisabled() {
    Text(
        modifier = Modifier.padding(16.dp),
        text = stringResource(R.string.battery_sync_disabled)
    )
}

@Composable
fun BatteryStatus(
    percent: Int,
    phoneName: String,
    onClick: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BatteryIcon(percent, modifier = Modifier.size(56.dp))
        Text(
            stringResource(R.string.battery_sync_hint_text, phoneName),
            style = MaterialTheme.typography.body1,
            textAlign = TextAlign.Center
        )
        Text(
            stringResource(R.string.battery_percent, percent.toString()),
            style = MaterialTheme.typography.display3,
            textAlign = TextAlign.Center
        )
    }
}
