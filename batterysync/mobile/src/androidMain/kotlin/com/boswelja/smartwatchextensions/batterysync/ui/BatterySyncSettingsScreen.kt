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
import com.boswelja.smartwatchextensions.batterysync.ui.BatterySliderDefaults.PROGRESS_FACTOR
import com.boswelja.smartwatchextensions.core.ui.settings.HeroSetting
import com.boswelja.smartwatchextensions.core.ui.settings.ShortcutSetting
import org.koin.androidx.compose.getViewModel
import kotlin.math.round

private const val BATTERY_CHARGE_MIN = 0.7f
private const val BATTERY_LOW_MIN = 0.05f
private const val BATTERY_LOW_MAX = 0.35f

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
            batterySyncEnabled = batterySyncEnabled,
            batteryStats = batteryStats,
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
    val phoneChargeNotiEnabled by viewModel.phoneChargeNotiEnabled.collectAsState()
    val phoneLowNotiEnabled by viewModel.phoneLowNotiEnabled.collectAsState()
    val watchChargeNotiEnabled by viewModel.watchChargeNotiEnabled.collectAsState()
    val watchLowNotiEnabled by viewModel.watchLowNotiEnabled.collectAsState()

    val settingsModifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth()
    Column(modifier) {
        HeroSetting(
            checked = batterySyncEnabled,
            onCheckedChange = viewModel::setBatterySyncEnabled,
            text = { Text(stringResource(R.string.battery_sync_toggle_title)) },
            modifier = settingsModifier
        )
        BatterySliderSetting(
            valueRange = BATTERY_CHARGE_MIN..1f,
            value = chargeThreshold / PROGRESS_FACTOR,
            onValueChanged = { viewModel.setChargeThreshold(round(it * PROGRESS_FACTOR).toInt()) },
            text = { Text(stringResource(R.string.battery_sync_charge_threshold_title)) },
            enabled = batterySyncEnabled,
            modifier = settingsModifier
        )
        BatterySliderSetting(
            valueRange = BATTERY_LOW_MIN..BATTERY_LOW_MAX,
            value = lowThreshold / PROGRESS_FACTOR,
            onValueChanged = { viewModel.setLowBatteryThreshold(round(it * PROGRESS_FACTOR).toInt()) },
            text = { Text(stringResource(R.string.battery_sync_low_threshold_title)) },
            enabled = batterySyncEnabled,
            modifier = settingsModifier
        )
        ShortcutSetting(
            text = { Text(stringResource(R.string.phone_battery_noti_title)) },
            summary = { Text(notificationSummaryText(phoneLowNotiEnabled, phoneChargeNotiEnabled)) },
            modifier = settingsModifier,
            enabled = batterySyncEnabled,
            onClick = {
                onNavigate(BatterySyncDestinations.PHONE_BATTERY_NOTIFICATION_SETTINGS.route)
            }
        )
        ShortcutSetting(
            text = { Text(stringResource(R.string.watch_battery_noti_title)) },
            summary = { Text(notificationSummaryText(watchLowNotiEnabled, watchChargeNotiEnabled)) },
            modifier = settingsModifier,
            enabled = batterySyncEnabled,
            onClick = { onNavigate(BatterySyncDestinations.WATCH_BATTERY_NOTIFICATION_SETTINGS.route) }
        )
    }
}

/**
 * Gets the appropriate battery notification summary text for the provided parameters.
 * @param lowNotificationEnabled Whether low notifications are enabled.
 * @param chargeNotificationEnabled Whether charge notifications are enabled.
 */
@Composable
fun notificationSummaryText(lowNotificationEnabled: Boolean, chargeNotificationEnabled: Boolean): String {
    return if (lowNotificationEnabled && chargeNotificationEnabled) {
        stringResource(R.string.battery_noti_low_and_charged)
    } else if (chargeNotificationEnabled) {
        stringResource(R.string.battery_noti_charged)
    } else if (lowNotificationEnabled) {
        stringResource(R.string.battery_noti_low)
    } else {
        stringResource(R.string.battery_noti_none)
    }
}
