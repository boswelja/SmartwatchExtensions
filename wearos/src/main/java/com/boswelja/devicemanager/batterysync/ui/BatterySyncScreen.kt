package com.boswelja.devicemanager.batterysync.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.BatteryIcon

@ExperimentalMaterialApi
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

@ExperimentalMaterialApi
@Composable
fun BatterySyncDisabled() {
    ListItem(
        text = { Text(stringResource(R.string.battery_sync_disabled)) },
        icon = { BatteryIcon(percent = -1, modifier = Modifier.size(32.dp)) }
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
            style = MaterialTheme.typography.body1
        )
        Text(
            stringResource(R.string.battery_percent, percent.toString()),
            style = MaterialTheme.typography.h6
        )
    }
}
