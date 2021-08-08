package com.boswelja.smartwatchextensions.batterysync.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.ui.CheckboxSetting
import com.boswelja.smartwatchextensions.common.ui.HeaderItem
import com.boswelja.smartwatchextensions.common.ui.SliderSetting
import com.boswelja.smartwatchextensions.common.ui.SwitchSetting

@Composable
fun BatterySyncSettingsScreen() {
    val viewModel: BatterySyncViewModel = viewModel()
    val batterySyncEnabled by viewModel.batterySyncEnabled.collectAsState(false)
    val scrollState = rememberScrollState()
    Column(
        Modifier.verticalScroll(scrollState)
    ) {
        BatterySyncSettings()
        Divider()
        ChargeNotificationSettings(
            isEnabled = batterySyncEnabled
        )
        Divider()
        LowBatteryNotificationSettings(
            isEnabled = batterySyncEnabled
        )
    }
}

@Composable
fun BatterySyncSettings(
    modifier: Modifier = Modifier
) {
    val viewModel: BatterySyncViewModel = viewModel()

    val canSyncBattery by viewModel.canSyncBattery.collectAsState(false)
    val batterySyncEnabled by viewModel.batterySyncEnabled.collectAsState(false)

    Column(modifier) {
        HeaderItem(
            text = { Text(stringResource(R.string.category_battery_sync_settings)) }
        )
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

@Composable
fun ChargeNotificationSettings(
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true
) {
    val viewModel: BatterySyncViewModel = viewModel()

    val phoneChargeNotiEnabled by viewModel.phoneChargeNotiEnabled.collectAsState(false)
    val watchChargeNotiEnabled by viewModel.watchChargeNotiEnabled.collectAsState(false)
    val chargeThreshold by viewModel.chargeThreshold.collectAsState(90)
    var currentThreshold by remember {
        mutableStateOf(chargeThreshold / 100f)
    }

    Column(modifier) {
        HeaderItem(
            text = { Text(stringResource(R.string.category_charge_notifications)) }
        )
        CheckboxSetting(
            label = { Text(stringResource(R.string.battery_sync_phone_charge_noti_title)) },
            summary = {
                val text = stringResource(
                    R.string.battery_sync_phone_charge_noti_summary,
                    (currentThreshold * 100).toInt().toString()
                )
                Text(text)
            },
            checked = phoneChargeNotiEnabled,
            enabled = isEnabled,
            onCheckChanged = {
                viewModel.setPhoneChargeNotiEnabled(it)
            }
        )
        CheckboxSetting(
            label = { Text(stringResource(R.string.battery_sync_watch_charge_noti_title)) },
            summary = {
                val text = stringResource(
                    R.string.battery_sync_watch_charge_noti_summary,
                    (currentThreshold * 100).toInt().toString()
                )
                Text(text)
            },
            checked = watchChargeNotiEnabled,
            enabled = isEnabled,
            onCheckChanged = {
                viewModel.setWatchChargeNotiEnabled(it)
            }
        )
        SliderSetting(
            label = { Text(stringResource(R.string.battery_sync_charge_threshold_title)) },
            valueRange = 0.6f..1f,
            value = currentThreshold,
            enabled = isEnabled,
            trailingFormat = stringResource(R.string.battery_percent),
            onSliderValueChanged = {
                currentThreshold = it
            },
            onSliderValueFinished = {
                viewModel.setChargeThreshold((currentThreshold * 100).toInt())
            }
        )
    }
}

@Composable
fun LowBatteryNotificationSettings(
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true
) {
    val viewModel: BatterySyncViewModel = viewModel()

    val phoneLowNotiEnabled by viewModel.phoneLowNotiEnabled.collectAsState(false)
    val watchLowNotiEnabled by viewModel.watchLowNotiEnabled.collectAsState(false)
    val batteryLowThreshold by viewModel.batteryLowThreshold.collectAsState(20)
    var currentThreshold by remember {
        mutableStateOf(batteryLowThreshold / 100f)
    }

    Column(modifier) {
        HeaderItem(
            text = { Text(stringResource(R.string.category_low_notifications)) }
        )
        CheckboxSetting(
            label = { Text(stringResource(R.string.battery_sync_phone_low_noti_title)) },
            summary = {
                val text = stringResource(
                    R.string.battery_sync_phone_low_noti_summary,
                    (currentThreshold * 100).toInt().toString()
                )
                Text(text)
            },
            enabled = isEnabled,
            checked = phoneLowNotiEnabled,
            onCheckChanged = {
                viewModel.setPhoneLowNotiEnabled(it)
            }
        )
        CheckboxSetting(
            label = { Text(stringResource(R.string.battery_sync_watch_low_noti_title)) },
            summary = {
                val text = stringResource(
                    R.string.battery_sync_watch_low_noti_summary,
                    (currentThreshold * 100).toInt().toString()
                )
                Text(text)
            },
            enabled = isEnabled,
            checked = watchLowNotiEnabled,
            onCheckChanged = {
                viewModel.setWatchLowNotiEnabled(it)
            }
        )
        SliderSetting(
            label = { Text(stringResource(R.string.battery_sync_low_threshold_title)) },
            valueRange = 0.05f..0.35f,
            value = currentThreshold,
            enabled = isEnabled,
            trailingFormat = stringResource(R.string.battery_percent),
            onSliderValueChanged = {
                currentThreshold = it
            },
            onSliderValueFinished = {
                viewModel.setLowBatteryThreshold((currentThreshold * 100).toInt())
            }
        )
    }
}
