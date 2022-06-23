package com.boswelja.smartwatchextensions.batterysync.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.boswelja.smartwatchextensions.batterysync.R
import com.boswelja.smartwatchextensions.batterysync.domain.model.DeviceBatteryNotificationState
import com.boswelja.smartwatchextensions.batterysync.ui.BatterySliderDefaults.PROGRESS_FACTOR
import com.boswelja.smartwatchextensions.core.ui.settings.HeroSetting
import com.boswelja.smartwatchextensions.core.ui.settings.ShortcutSetting
import org.koin.androidx.compose.getViewModel
import kotlin.math.round

private const val BatteryChargeMin = 0.7f
private const val BatteryLowMin = 0.05f
private const val BatteryLowMax = 0.35f

/**
 * A Composable screen for displaying Battery Sync settings.
 * @param modifier [Modifier].
 */
@Composable
fun BatterySyncSettingsScreen(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: BatterySyncViewModel = getViewModel()

    val canSyncBattery by viewModel.canSyncBattery.collectAsState()
    val batterySyncEnabled by viewModel.batterySyncEnabled.collectAsState()
    val batteryStats by viewModel.batteryStats.collectAsState()

    val scrollState = rememberScrollState()
    Column(
        Modifier
            .verticalScroll(scrollState)
            .then(modifier)
    ) {
        BatterySyncSettingsHeader(
            batterySyncEnabled = batterySyncEnabled == true,
            batteryStats = batteryStats?.data,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
        if (canSyncBattery) {
            BatterySyncSettings(
                viewModel = viewModel,
                onNavigate = onNavigate
            )
        } else {
            Text(stringResource(R.string.battery_sync_not_supported))
        }
    }
}

/**
 * A Composable to display available Battery Sync settings.
 */
@Composable
fun BatterySyncSettings(
    viewModel: BatterySyncViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val batterySyncEnabled by viewModel.batterySyncEnabled.collectAsState()
    val chargeThreshold by viewModel.chargeThreshold.collectAsState()
    val lowThreshold by viewModel.batteryLowThreshold.collectAsState()
    val watchBatteryNotiState by viewModel.watchBatteryNotiState.collectAsState()
    val phoneBatteryNotiState by viewModel.phoneBatteryNotiState.collectAsState()

    val settingsModifier = Modifier.fillMaxWidth()
    Column(modifier) {
        HeroSetting(
            checked = batterySyncEnabled == true,
            onCheckedChange = viewModel::setBatterySyncEnabled,
            text = { Text(stringResource(R.string.battery_sync_toggle_title)) },
            modifier = settingsModifier
        )
        BatterySliderSetting(
            valueRange = BatteryChargeMin..1f,
            value = chargeThreshold / PROGRESS_FACTOR,
            onValueChanged = { viewModel.setChargeThreshold(round(it * PROGRESS_FACTOR).toInt()) },
            text = { Text(stringResource(R.string.battery_sync_charge_threshold_title)) },
            enabled = batterySyncEnabled == true,
            modifier = settingsModifier
        )
        BatterySliderSetting(
            valueRange = BatteryLowMin..BatteryLowMax,
            value = lowThreshold / PROGRESS_FACTOR,
            onValueChanged = { viewModel.setLowBatteryThreshold(round(it * PROGRESS_FACTOR).toInt()) },
            text = { Text(stringResource(R.string.battery_sync_low_threshold_title)) },
            enabled = batterySyncEnabled == true,
            modifier = settingsModifier
        )
        ShortcutSetting(
            text = { Text(stringResource(R.string.phone_battery_noti_title)) },
            summary = { Text(phoneBatteryNotiState.notificationSummaryText()) },
            modifier = settingsModifier,
            enabled = batterySyncEnabled == true,
            onClick = {
                onNavigate(BatterySyncDestinations.PHONE_BATTERY_NOTIFICATION_SETTINGS.route)
            }
        )
        ShortcutSetting(
            text = { Text(stringResource(R.string.watch_battery_noti_title)) },
            summary = { Text(watchBatteryNotiState.notificationSummaryText()) },
            modifier = settingsModifier,
            enabled = batterySyncEnabled == true,
            onClick = { onNavigate(BatterySyncDestinations.WATCH_BATTERY_NOTIFICATION_SETTINGS.route) }
        )
    }
}

/**
 * Gets the appropriate battery notification summary text for the provided parameters.
 */
@Composable
fun DeviceBatteryNotificationState.notificationSummaryText(): String {
    return if (lowNotificationsEnabled && chargeNotificationsEnabled) {
        stringResource(R.string.battery_noti_low_and_charged)
    } else if (chargeNotificationsEnabled) {
        stringResource(R.string.battery_noti_charged)
    } else if (lowNotificationsEnabled) {
        stringResource(R.string.battery_noti_low)
    } else {
        stringResource(R.string.battery_noti_none)
    }
}
