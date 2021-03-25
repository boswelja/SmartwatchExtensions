package com.boswelja.devicemanager.batterysync.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.appmanager.ui.HeaderItem
import com.boswelja.devicemanager.common.ui.CheckboxPreference
import com.boswelja.devicemanager.common.ui.SliderPreference
import com.boswelja.devicemanager.common.ui.SwitchPreference

@ExperimentalMaterialApi
@Composable
fun BatterySyncSettingsScreen() {
    Column {
        BatterySyncSettings()
        Divider()
        ChargeNotificationSettings()
    }
}

@ExperimentalMaterialApi
@Composable
fun BatterySyncSettings() {
    val viewModel: BatterySyncViewModel = viewModel()

    val batterySyncEnabled by viewModel.batterySyncEnabled.observeAsState()
    val syncInterval by viewModel.syncInterval.observeAsState()
    var currentInterval by remember {
        mutableStateOf((syncInterval ?: 15) / 100f)
    }

    Column {
        HeaderItem(stringResource(R.string.category_battery_sync_settings))
        SwitchPreference(
            text = stringResource(R.string.battery_sync_toggle_title),
            isChecked = batterySyncEnabled == true,
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

    val phoneChargeNotiEnabled by viewModel.phoneChargeNotiEnabled.observeAsState()
    val watchChargeNotiEnabled by viewModel.watchChargeNotiEnabled.observeAsState()
    val chargeThreshold by viewModel.chargeThreshold.observeAsState()
    var currentThreshold by remember {
        mutableStateOf((chargeThreshold ?: 90) / 100f)
    }

    Column {
        HeaderItem(stringResource(R.string.category_charge_notifications))
        CheckboxPreference(
            text = stringResource(R.string.battery_sync_phone_charge_noti_title),
            secondaryText = stringResource(
                R.string.battery_sync_phone_charge_noti_summary,
                (currentThreshold * 100).toInt().toString()
            ),
            isChecked = phoneChargeNotiEnabled == true,
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
            isChecked = watchChargeNotiEnabled == true,
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
