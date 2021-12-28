package com.boswelja.smartwatchextensions.batterysync.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
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
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.ui.CheckboxSetting
import com.boswelja.smartwatchextensions.common.ui.SliderSetting
import com.boswelja.smartwatchextensions.common.ui.SwitchSetting
import org.koin.androidx.compose.getViewModel
import kotlin.math.round

private const val PROGRESS_FACTOR = 100f
private const val BATTERY_CHARGE_MIN = 0.6f
private const val BATTERY_LOW_MAX = 0.35f

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
    val viewModel: BatterySyncViewModel = getViewModel()

    val canSyncBattery by viewModel.canSyncBattery.collectAsState()
    val batterySyncEnabled by viewModel.batterySyncEnabled.collectAsState()
    val phoneChargeNotiEnabled by viewModel.phoneChargeNotiEnabled.collectAsState()
    val watchChargeNotiEnabled by viewModel.watchChargeNotiEnabled.collectAsState()
    val chargeThreshold by viewModel.chargeThreshold.collectAsState()
    val phoneLowNotiEnabled by viewModel.phoneLowNotiEnabled.collectAsState()
    val watchLowNotiEnabled by viewModel.watchLowNotiEnabled.collectAsState()
    val lowThreshold by viewModel.batteryLowThreshold.collectAsState()

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = contentPadding)
    ) {
        item {
            BatterySyncSettingsHeader()
            Divider()
        }
        if (canSyncBattery) {
            batterySyncSettings(
                batterySyncEnabled,
                viewModel::setBatterySyncEnabled
            )
            chargeNotificationSettings(
                phoneChargeNotiEnabled,
                watchChargeNotiEnabled,
                chargeThreshold,
                viewModel::setPhoneChargeNotiEnabled,
                viewModel::setWatchChargeNotiEnabled,
                viewModel::setChargeThreshold
            )
            lowNotificationSettings(
                phoneLowNotiEnabled,
                watchLowNotiEnabled,
                lowThreshold,
                viewModel::setPhoneLowNotiEnabled,
                viewModel::setWatchLowNotiEnabled,
                viewModel::setLowBatteryThreshold
            )
        } else {
            item {
                Text(text = stringResource(id = R.string.capability_not_supported))
            }
        }
    }
}

/**
 * A Composable to display Battery Sync settings.
 */
@OptIn(ExperimentalMaterialApi::class)
fun LazyListScope.batterySyncSettings(
    batterySyncEnabled: Boolean,
    onBatterySyncEnabledChanged: (Boolean) -> Unit
) {
    item {
        SwitchSetting(
            label = { Text(stringResource(R.string.battery_sync_toggle_title)) },
            checked = batterySyncEnabled,
            onCheckChanged = onBatterySyncEnabledChanged
        )
    }
}

/**
 * A Composable for displaying charge notification settings.
 */
fun LazyListScope.chargeNotificationSettings(
    phoneChargeNotiEnabled: Boolean,
    watchChargeNotiEnabled: Boolean,
    chargeThreshold: Int,
    onPhoneChargeNotiEnabledChanged: (Boolean) -> Unit,
    onWatchChargeNotiEnabledChanged: (Boolean) -> Unit,
    onChargeThresholdChanged: (Int) -> Unit,
) {
    item {
        CheckboxSetting(
            label = { Text(stringResource(R.string.battery_sync_phone_charge_noti_title)) },
            summary = {
                val text = stringResource(
                    R.string.battery_sync_phone_charge_noti_summary,
                    chargeThreshold
                )
                Text(text)
            },
            checked = phoneChargeNotiEnabled,
            onCheckChanged = onPhoneChargeNotiEnabledChanged
        )
    }
    item {
        CheckboxSetting(
            label = { Text(stringResource(R.string.battery_sync_watch_charge_noti_title)) },
            summary = {
                val text = stringResource(
                    R.string.battery_sync_watch_charge_noti_summary,
                    chargeThreshold
                )
                Text(text)
            },
            checked = watchChargeNotiEnabled,
            onCheckChanged = onWatchChargeNotiEnabledChanged
        )
    }
    item {
        var currentThreshold by remember(chargeThreshold) { mutableStateOf(chargeThreshold / PROGRESS_FACTOR) }
        SliderSetting(
            label = { Text(stringResource(R.string.battery_sync_charge_threshold_title)) },
            trailing = { value ->
                Text(stringResource(R.string.battery_percent, round(value * PROGRESS_FACTOR)))
            },
            valueRange = BATTERY_CHARGE_MIN..1f,
            value = currentThreshold,
            onSliderValueChanged = {
                currentThreshold = it
            },
            onSliderValueFinished = {
                onChargeThresholdChanged(round(currentThreshold * PROGRESS_FACTOR).toInt())
            }
        )
    }
}

/**
 * A Composable for displaying low battery notification settings.
 */
fun LazyListScope.lowNotificationSettings(
    phoneLowNotiEnabled: Boolean,
    watchLowNotiEnabled: Boolean,
    lowThreshold: Int,
    onPhoneLowNotiEnabledChanged: (Boolean) -> Unit,
    onWatchLowNotiEnabledChanged: (Boolean) -> Unit,
    onLowThresholdChanged: (Int) -> Unit,
) {
    item {
        CheckboxSetting(
            label = { Text(stringResource(R.string.battery_sync_phone_low_noti_title)) },
            summary = {
                val text = stringResource(
                    R.string.battery_sync_phone_low_noti_summary,
                    lowThreshold
                )
                Text(text)
            },
            checked = phoneLowNotiEnabled,
            onCheckChanged = onPhoneLowNotiEnabledChanged
        )
    }
    item {
        CheckboxSetting(
            label = { Text(stringResource(R.string.battery_sync_watch_low_noti_title)) },
            summary = {
                val text = stringResource(
                    R.string.battery_sync_watch_low_noti_summary,
                    lowThreshold
                )
                Text(text)
            },
            checked = watchLowNotiEnabled,
            onCheckChanged = onWatchLowNotiEnabledChanged
        )
    }
    item {
        var currentThreshold by remember(lowThreshold) { mutableStateOf(lowThreshold / PROGRESS_FACTOR) }
        SliderSetting(
            label = { Text(stringResource(R.string.battery_sync_low_threshold_title)) },
            trailing = { value ->
                Text(stringResource(R.string.battery_percent, round(value * PROGRESS_FACTOR)))
            },
            valueRange = 0.05f..BATTERY_LOW_MAX,
            value = currentThreshold,
            onSliderValueChanged = {
                currentThreshold = it
            },
            onSliderValueFinished = {
                onLowThresholdChanged(round(currentThreshold * PROGRESS_FACTOR).toInt())
            }
        )
    }
}
