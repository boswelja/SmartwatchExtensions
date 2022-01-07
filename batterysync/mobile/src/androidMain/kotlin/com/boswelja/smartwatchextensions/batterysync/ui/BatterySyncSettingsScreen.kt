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
            BatterySyncSettings(
                batterySyncEnabled = batterySyncEnabled,
                onBatterySyncEnabledChanged = viewModel::setBatterySyncEnabled
            )
            ListItem(
                text = { Text("Phone Battery Notifications") },
                modifier = Modifier.clickable {
                    onNavigate(BatterySyncDestinations.PHONE_BATTERY_NOTIFICATION_SETTINGS.route)
                }
            )
            ListItem(
                text = { Text("Watch Battery Notifications") },
                modifier = Modifier.clickable {
                    onNavigate(BatterySyncDestinations.WATCH_BATTERY_NOTIFICATION_SETTINGS.route)
                }
            )
            ChargeNotificationSettings(
                chargeThreshold = chargeThreshold,
                onChargeThresholdChanged = viewModel::setChargeThreshold
            )
            LowNotificationSettings(
                lowThreshold = lowThreshold,
                onLowThresholdChanged = viewModel::setLowBatteryThreshold
            )
        } else {
            Text("Your watch has indicated it doesn't support Battery Sync")
        }
    }
}

/**
 * A Composable to display Battery Sync settings.
 */
@Composable
fun BatterySyncSettings(
    batterySyncEnabled: Boolean,
    onBatterySyncEnabledChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    SwitchSetting(
        label = { Text(stringResource(R.string.battery_sync_toggle_title)) },
        checked = batterySyncEnabled,
        onCheckChanged = onBatterySyncEnabledChanged,
        modifier = modifier
    )
}

/**
 * A Composable for displaying charge notification settings.
 */
@Composable
fun ChargeNotificationSettings(
    chargeThreshold: Int,
    onChargeThresholdChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    settingModifier: Modifier = Modifier
) {
    Column(modifier) {
        BatterySliderSetting(
            valueRange = BATTERY_CHARGE_MIN..1f,
            value = chargeThreshold / PROGRESS_FACTOR,
            onValueChanged ={ onChargeThresholdChanged(round(it * PROGRESS_FACTOR).toInt()) },
            text = { Text(stringResource(R.string.battery_sync_charge_threshold_title)) },
            modifier = settingModifier
        )
    }
}

/**
 * A Composable for displaying low battery notification settings.
 */
@Composable
fun LowNotificationSettings(
    lowThreshold: Int,
    onLowThresholdChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    settingModifier: Modifier = Modifier
) {
    Column(modifier) {
        BatterySliderSetting(
            valueRange = BATTERY_LOW_MIN..BATTERY_LOW_MAX,
            value = lowThreshold / PROGRESS_FACTOR,
            onValueChanged = { onLowThresholdChanged(round(it * PROGRESS_FACTOR).toInt())},
            text = { Text(stringResource(R.string.battery_sync_low_threshold_title)) },
            modifier = settingModifier
        )
    }
}
