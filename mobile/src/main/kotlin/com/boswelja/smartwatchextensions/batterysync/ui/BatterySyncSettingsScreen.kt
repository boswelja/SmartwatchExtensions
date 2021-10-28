package com.boswelja.smartwatchextensions.batterysync.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.ui.Card
import com.boswelja.smartwatchextensions.common.ui.CardHeader
import com.boswelja.smartwatchextensions.common.ui.CheckboxSetting
import com.boswelja.smartwatchextensions.common.ui.SliderSetting
import com.boswelja.smartwatchextensions.common.ui.SwitchSetting

private const val PROGRESS_FACTOR = 100f
private const val BATTERY_CHARGE_MIN = 0.6f
private const val BATTERY_LOW_MAX = 0.35f
private const val BATTERY_CHARGE_DEFAULT = 90
private const val BATTERY_LOW_DEFAULT = 20

/**
 * A Composable screen for displaying Battery Sync settings.
 * @param modifier [Modifier].
 * @param contentPadding The padding around the screen content.
 */
@Composable
fun BatterySyncSettingsScreen(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp
) {
    val viewModel: BatterySyncViewModel = viewModel()
    val batterySyncEnabled by viewModel.batterySyncEnabled.collectAsState(false)
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(contentPadding),
        verticalArrangement = Arrangement.spacedBy(contentPadding)
    ) {
        item {
            BatterySyncSettingsHeader()
        }
        item {
            BatterySyncSettings()
        }
        item {
            ChargeNotificationSettings(
                isEnabled = batterySyncEnabled
            )
        }
        item {
            LowBatteryNotificationSettings(
                isEnabled = batterySyncEnabled
            )
        }
    }
}

/**
 * A Composable to display Battery Sync settings.
 * @param modifier [Modifier].
 */
@Composable
fun BatterySyncSettings(
    modifier: Modifier = Modifier
) {
    val viewModel: BatterySyncViewModel = viewModel()

    val canSyncBattery by viewModel.canSyncBattery.collectAsState(false)
    val batterySyncEnabled by viewModel.batterySyncEnabled.collectAsState(false)

    Card(
        modifier = modifier,
        header = {
            CardHeader(title = { Text(stringResource(R.string.category_battery_sync_settings)) })
        }
    ) {
        Column {
            SwitchSetting(
                label = { Text(stringResource(R.string.battery_sync_toggle_title)) },
                summary = if (!canSyncBattery) {
                    { Text(stringResource(R.string.capability_not_supported)) }
                } else null,
                checked = batterySyncEnabled,
                onCheckChanged = {
                    viewModel.setBatterySyncEnabled(it)
                },
                enabled = canSyncBattery
            )
        }
    }
}

/**
 * A Composable for displaying charge notification settings.
 * @param modifier [Modifier].
 * @param isEnabled Whether all settings in this group are enabled.
 */
@Composable
fun ChargeNotificationSettings(
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true
) {
    val viewModel: BatterySyncViewModel = viewModel()

    val phoneChargeNotiEnabled by viewModel.phoneChargeNotiEnabled.collectAsState(false)
    val watchChargeNotiEnabled by viewModel.watchChargeNotiEnabled.collectAsState(false)
    val chargeThreshold by viewModel.chargeThreshold.collectAsState(BATTERY_CHARGE_DEFAULT)
    var currentThreshold by remember { mutableStateOf(chargeThreshold / PROGRESS_FACTOR) }

    Card(
        modifier = modifier,
        header = { CardHeader(title = { Text(stringResource(R.string.category_charge_notifications)) }) }
    ) {
        Column {
            CheckboxSetting(
                label = { Text(stringResource(R.string.battery_sync_phone_charge_noti_title)) },
                summary = {
                    val text = stringResource(
                        R.string.battery_sync_phone_charge_noti_summary,
                        (currentThreshold * PROGRESS_FACTOR).toInt().toString()
                    )
                    Text(text)
                },
                checked = phoneChargeNotiEnabled,
                enabled = isEnabled,
                onCheckChanged = { viewModel.setPhoneChargeNotiEnabled(it) }
            )
            CheckboxSetting(
                label = { Text(stringResource(R.string.battery_sync_watch_charge_noti_title)) },
                summary = {
                    val text = stringResource(
                        R.string.battery_sync_watch_charge_noti_summary,
                        (currentThreshold * PROGRESS_FACTOR).toInt().toString()
                    )
                    Text(text)
                },
                checked = watchChargeNotiEnabled,
                enabled = isEnabled,
                onCheckChanged = { viewModel.setWatchChargeNotiEnabled(it) }
            )
            SliderSetting(
                label = { Text(stringResource(R.string.battery_sync_charge_threshold_title)) },
                trailing = { value ->
                    Text(stringResource(R.string.battery_percent, value * PROGRESS_FACTOR))
                },
                valueRange = BATTERY_CHARGE_MIN..1f,
                value = currentThreshold,
                enabled = isEnabled,
                onSliderValueChanged = {
                    currentThreshold = it
                },
                onSliderValueFinished = {
                    viewModel.setChargeThreshold((currentThreshold * PROGRESS_FACTOR).toInt())
                }
            )
        }
    }
}

/**
 * A Composable for displaying low battery notification settings.
 * @param modifier [Modifier].
 * @param isEnabled Whether all settings in this group are enabled.
 */
@Composable
fun LowBatteryNotificationSettings(
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true
) {
    val viewModel: BatterySyncViewModel = viewModel()

    val phoneLowNotiEnabled by viewModel.phoneLowNotiEnabled.collectAsState(false)
    val watchLowNotiEnabled by viewModel.watchLowNotiEnabled.collectAsState(false)
    val batteryLowThreshold by viewModel.batteryLowThreshold.collectAsState(BATTERY_LOW_DEFAULT)
    var currentThreshold by remember { mutableStateOf(batteryLowThreshold / PROGRESS_FACTOR) }

    Card(
        modifier = modifier,
        header = { CardHeader(title = { Text(stringResource(R.string.category_low_notifications)) }) }
    ) {
        Column {
            CheckboxSetting(
                label = { Text(stringResource(R.string.battery_sync_phone_low_noti_title)) },
                summary = {
                    val text = stringResource(
                        R.string.battery_sync_phone_low_noti_summary,
                        (currentThreshold * PROGRESS_FACTOR).toInt().toString()
                    )
                    Text(text)
                },
                enabled = isEnabled,
                checked = phoneLowNotiEnabled,
                onCheckChanged = { viewModel.setPhoneLowNotiEnabled(it) }
            )
            CheckboxSetting(
                label = { Text(stringResource(R.string.battery_sync_watch_low_noti_title)) },
                summary = {
                    val text = stringResource(
                        R.string.battery_sync_watch_low_noti_summary,
                        (currentThreshold * PROGRESS_FACTOR).toInt().toString()
                    )
                    Text(text)
                },
                enabled = isEnabled,
                checked = watchLowNotiEnabled,
                onCheckChanged = { viewModel.setWatchLowNotiEnabled(it) }
            )
            SliderSetting(
                label = { Text(stringResource(R.string.battery_sync_low_threshold_title)) },
                trailing = { value ->
                    Text(stringResource(R.string.battery_percent, value * PROGRESS_FACTOR))
                },
                valueRange = 0f..BATTERY_LOW_MAX,
                value = currentThreshold,
                enabled = isEnabled,
                onSliderValueChanged = { currentThreshold = it },
                onSliderValueFinished = {
                    viewModel.setLowBatteryThreshold((currentThreshold * PROGRESS_FACTOR).toInt())
                }
            )
        }
    }
}
