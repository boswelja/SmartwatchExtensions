package com.boswelja.smartwatchextensions.batterysync.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.batterysync.database.WatchBatteryStats
import com.boswelja.smartwatchextensions.common.BatteryIcon
import java.util.concurrent.TimeUnit

@Composable
fun BatterySyncSettingsHeader() {
    val viewModel: BatterySyncViewModel = viewModel()
    val batterySyncEnabled by viewModel.batterySyncEnabled.observeAsState()
    if (batterySyncEnabled == true) {
        val batteryStats by viewModel.batteryStats.observeAsState()
        batteryStats.let {
            if (it != null) {
                BatteryStats(it)
            } else {
                BatterySyncStatus(stringResource(R.string.please_wait))
            }
        }
    } else {
        BatterySyncStatus(stringResource(R.string.battery_sync_disabled))
    }
}

@Composable
fun BatteryStats(batteryStats: WatchBatteryStats) {
    val dataAgeMinutes =
        TimeUnit.MILLISECONDS
            .toMinutes(System.currentTimeMillis() - batteryStats.lastUpdatedMillis)
            .toInt()
    Row(
        Modifier
            .fillMaxWidth()
            .aspectRatio(3f)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.battery_percent, batteryStats.percent.toString()),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.h5,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = if (dataAgeMinutes > 1)
                    LocalContext.current.resources.getQuantityString(
                        R.plurals.battery_sync_last_updated_minutes, dataAgeMinutes, dataAgeMinutes
                    )
                else stringResource(R.string.battery_sync_last_update_recent),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.body1,
                modifier = Modifier.fillMaxWidth()
            )
        }

        BatteryIcon(
            percent = batteryStats.percent,
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1f)
        )
    }
}

@Composable
fun BatterySyncStatus(statusText: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .aspectRatio(3f)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = statusText,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.h5,
            modifier = Modifier.weight(1f)
        )
        BatteryIcon(
            percent = -1,
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1f)
        )
    }
}
