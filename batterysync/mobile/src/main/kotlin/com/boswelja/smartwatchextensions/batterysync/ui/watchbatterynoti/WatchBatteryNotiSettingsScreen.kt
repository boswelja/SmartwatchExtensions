package com.boswelja.smartwatchextensions.batterysync.ui.watchbatterynoti

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.boswelja.smartwatchextensions.batterysync.R
import com.boswelja.smartwatchextensions.core.ui.settings.CheckboxSetting
import org.koin.compose.viewmodel.koinViewModel

/**
 * A Composable screen to let the user control notifications related to watch battery.
 * @param modifier [Modifier].
 */
@Composable
fun WatchBatteryNotiSettingsScreen(
    modifier: Modifier = Modifier
) {
    val viewModel: WatchBatteryNotiSettingsViewModel = koinViewModel()

    val watchChargeNotiEnabled by viewModel.watchChargeNotiEnabled.collectAsState()
    val watchLowNotiEnabled by viewModel.watchLowNotiEnabled.collectAsState()

    val chargeThreshold by viewModel.chargeThreshold.collectAsState()
    val lowThreshold by viewModel.batteryLowThreshold.collectAsState()

    val settingsModifier = Modifier.fillMaxWidth()
    Column(modifier) {
        CheckboxSetting(
            text = { Text(stringResource(R.string.battery_sync_watch_charge_noti_title)) },
            summary = {
                val text = stringResource(
                    R.string.battery_sync_watch_charge_noti_summary,
                    chargeThreshold
                )
                Text(text)
            },
            checked = watchChargeNotiEnabled,
            onCheckedChange = viewModel::setWatchChargeNotiEnabled,
            modifier = settingsModifier
        )
        CheckboxSetting(
            text = { Text(stringResource(R.string.battery_sync_watch_low_noti_title)) },
            summary = {
                val text = stringResource(R.string.battery_sync_watch_low_noti_summary, lowThreshold)
                Text(text)
            },
            checked = watchLowNotiEnabled,
            onCheckedChange = viewModel::setWatchLowNotiEnabled,
            modifier = settingsModifier
        )
    }
}
