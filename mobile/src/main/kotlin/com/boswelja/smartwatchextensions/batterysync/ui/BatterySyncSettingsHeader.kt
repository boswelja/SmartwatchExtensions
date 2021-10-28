package com.boswelja.smartwatchextensions.batterysync.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.BatteryIcon
import kotlinx.coroutines.Dispatchers

private const val HEADER_ASPECT_RATIO = 3f

/**
 * Display a header for Battery Sync settings.
 */
@Composable
fun BatterySyncSettingsHeader() {
    val viewModel: BatterySyncViewModel = viewModel()
    val batterySyncEnabled by viewModel.batterySyncEnabled.collectAsState(false)
    if (batterySyncEnabled) {
        val batteryStats by viewModel.batteryStats.collectAsState(null, Dispatchers.IO)
        batteryStats.let {
            if (it != null) {
                BatterySummaryLarge(
                    Modifier.fillMaxWidth().aspectRatio(HEADER_ASPECT_RATIO).padding(16.dp),
                    it
                )
            } else {
                BatterySyncStatus(stringResource(R.string.please_wait))
            }
        }
    } else {
        BatterySyncStatus(stringResource(R.string.battery_sync_disabled))
    }
}

/**
 * Display the Battery Sync status.
 */
@Composable
fun BatterySyncStatus(statusText: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .aspectRatio(HEADER_ASPECT_RATIO)
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
