package com.boswelja.smartwatchextensions.batterysync.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.boswelja.smartwatchextensions.batterysync.R
import com.boswelja.smartwatchextensions.batterysync.ui.BatterySliderDefaults.PROGRESS_FACTOR
import com.boswelja.smartwatchextensions.settings.ui.SwitchSetting
import org.koin.androidx.compose.getViewModel
import kotlin.math.round

private const val BATTERY_CHARGE_MIN = 0.7f
private const val BATTERY_LOW_MIN = 0.05f
private const val BATTERY_LOW_MAX = 0.35f

/**
 * A Composable screen for displaying Battery Sync settings.
 * @param modifier [Modifier].
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BatterySyncSettingsScreen(
    modifier: Modifier = Modifier,
    onNavigate: (String) -> Unit
) {
    val viewModel: BatterySyncViewModel = getViewModel()

    val canSyncBattery by viewModel.canSyncBattery.collectAsState()
    val batterySyncEnabled by viewModel.batterySyncEnabled.collectAsState()
    val chargeThreshold by viewModel.chargeThreshold.collectAsState()
    val lowThreshold by viewModel.batteryLowThreshold.collectAsState()

    val scrollState = rememberScrollState()
    Column(
        Modifier
            .verticalScroll(scrollState)
            .then(modifier)
    ) {
        BatterySyncSettingsHeader()
        Divider()
        if (canSyncBattery) {
            SwitchSetting(
                label = { Text(stringResource(R.string.battery_sync_toggle_title)) },
                checked = batterySyncEnabled,
                onCheckChanged = viewModel::setBatterySyncEnabled
            )
            BatterySliderSetting(
                valueRange = BATTERY_CHARGE_MIN..1f,
                value = chargeThreshold / PROGRESS_FACTOR,
                onValueChanged = { viewModel.setChargeThreshold(round(it * PROGRESS_FACTOR).toInt()) },
                text = { Text(stringResource(R.string.battery_sync_charge_threshold_title)) }
            )
            BatterySliderSetting(
                valueRange = BATTERY_LOW_MIN..BATTERY_LOW_MAX,
                value = lowThreshold / PROGRESS_FACTOR,
                onValueChanged = { viewModel.setLowBatteryThreshold(round(it * PROGRESS_FACTOR).toInt()) },
                text = { Text(stringResource(R.string.battery_sync_low_threshold_title)) }
            )
            ListItem(
                text = { Text(stringResource(R.string.phone_battery_noti_title)) },
                modifier = Modifier.clickable {
                    onNavigate(BatterySyncDestinations.PHONE_BATTERY_NOTIFICATION_SETTINGS.route)
                }
            )
            ListItem(
                text = { Text(stringResource(R.string.watch_battery_noti_title)) },
                modifier = Modifier.clickable {
                    onNavigate(BatterySyncDestinations.WATCH_BATTERY_NOTIFICATION_SETTINGS.route)
                }
            )
        } else {
            Text(stringResource(R.string.battery_sync_not_supported))
        }
    }
}
