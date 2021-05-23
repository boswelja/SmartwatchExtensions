package com.boswelja.smartwatchextensions.batterysync.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
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
import com.boswelja.smartwatchextensions.common.ui.CheckboxPreference
import com.boswelja.smartwatchextensions.common.ui.HeaderItem
import com.boswelja.smartwatchextensions.common.ui.SliderPreference
import com.boswelja.smartwatchextensions.common.ui.SwitchPreference

@ExperimentalMaterialApi
@Composable
fun BatterySyncSettingsScreen() {
    val scrollState = rememberScrollState()
    Column(
        Modifier.verticalScroll(scrollState)
    ) {
        BatterySyncSettings()
        Divider()
        ChargeNotificationSettings()
        Divider()
        LowBatteryNotificationSettings()
    }
}

@ExperimentalMaterialApi
@Composable
fun BatterySyncSettings() {
    val viewModel: BatterySyncViewModel = viewModel()

    val batterySyncEnabled by viewModel.batterySyncEnabled.collectAsState(false)
    val syncInterval by viewModel.syncInterval.collectAsState(15)
    var currentInterval by remember {
        mutableStateOf(syncInterval / 100f)
    }

    Column {
        HeaderItem(stringResource(R.string.category_battery_sync_settings))
        SwitchPreference(
            text = stringResource(R.string.battery_sync_toggle_title),
            isChecked = batterySyncEnabled,
            onCheckChanged = {
                viewModel.setBatterySyncEnabled(it)
            }
        )
        SliderPreference(
            text = stringResource(R.string.battery_sync_interval_title),
            value = currentInterval,
            valueRange = 0.15f..0.60f,
            trailingFormat = stringResource(R.string.battery_sync_interval),
            onSliderValueChanged = {
                currentInterval = it
            },
            onSliderValueFinished = {
                viewModel.setSyncInterval((currentInterval * 100).toInt())
            }
        )
    }
}

@ExperimentalMaterialApi
@Composable
fun ChargeNotificationSettings() {
    val viewModel: BatterySyncViewModel = viewModel()

    val phoneChargeNotiEnabled by viewModel.phoneChargeNotiEnabled.collectAsState(false)
    val watchChargeNotiEnabled by viewModel.watchChargeNotiEnabled.collectAsState(false)
    val chargeThreshold by viewModel.chargeThreshold.collectAsState(90)
    var currentThreshold by remember {
        mutableStateOf(chargeThreshold / 100f)
    }

    Column {
        HeaderItem(stringResource(R.string.category_charge_notifications))
        CheckboxPreference(
            text = stringResource(R.string.battery_sync_phone_charge_noti_title),
            secondaryText = stringResource(
                R.string.battery_sync_phone_charge_noti_summary,
                (currentThreshold * 100).toInt().toString()
            ),
            isChecked = phoneChargeNotiEnabled,
            onCheckChanged = {
                viewModel.setPhoneChargeNotiEnabled(it)
            }
        )
        CheckboxPreference(
            text = stringResource(R.string.battery_sync_watch_charge_noti_title),
            secondaryText = stringResource(
                R.string.battery_sync_watch_charge_noti_summary,
                (currentThreshold * 100).toInt().toString()
            ),
            isChecked = watchChargeNotiEnabled,
            onCheckChanged = {
                viewModel.setWatchChargeNotiEnabled(it)
            }
        )
        SliderPreference(
            text = stringResource(R.string.battery_sync_charge_threshold_title),
            valueRange = 0.6f..1f,
            value = currentThreshold,
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

@ExperimentalMaterialApi
@Composable
fun LowBatteryNotificationSettings() {
    val viewModel: BatterySyncViewModel = viewModel()

    val phoneLowNotiEnabled by viewModel.phoneLowNotiEnabled.collectAsState(false)
    val watchLowNotiEnabled by viewModel.watchLowNotiEnabled.collectAsState(false)
    val batteryLowThreshold by viewModel.batteryLowThreshold.collectAsState(20)
    var currentThreshold by remember {
        mutableStateOf(batteryLowThreshold / 100f)
    }

    Column {
        HeaderItem(stringResource(R.string.category_low_notifications))
        CheckboxPreference(
            text = stringResource(R.string.battery_sync_phone_low_noti_title),
            secondaryText = stringResource(
                R.string.battery_sync_phone_low_noti_summary,
                (currentThreshold * 100).toInt().toString()
            ),
            isChecked = phoneLowNotiEnabled,
            onCheckChanged = {
                viewModel.setPhoneLowNotiEnabled(it)
            }
        )
        CheckboxPreference(
            text = stringResource(R.string.battery_sync_watch_low_noti_title),
            secondaryText = stringResource(
                R.string.battery_sync_watch_low_noti_summary,
                (currentThreshold * 100).toInt().toString()
            ),
            isChecked = watchLowNotiEnabled,
            onCheckChanged = {
                viewModel.setWatchLowNotiEnabled(it)
            }
        )
        SliderPreference(
            text = stringResource(R.string.battery_sync_low_threshold_title),
            valueRange = 0.05f..0.35f,
            value = currentThreshold,
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
