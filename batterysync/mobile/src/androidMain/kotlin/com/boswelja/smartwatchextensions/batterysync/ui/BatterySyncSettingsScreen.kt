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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.boswelja.smartwatchextensions.batterysync.R
import com.boswelja.smartwatchextensions.batterysync.ui.BatterySliderDefaults.PROGRESS_FACTOR
import com.boswelja.smartwatchextensions.settings.ui.CheckboxSetting
import com.boswelja.smartwatchextensions.settings.ui.SwitchSetting
import org.koin.androidx.compose.getViewModel
import kotlin.math.round

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
                Text("Your watch has indicated it doesn't support Battery Sync")
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
        BatterySliderSetting(
            valueRange = BATTERY_CHARGE_MIN..1f,
            value = chargeThreshold / PROGRESS_FACTOR,
            onValueChanged ={ onChargeThresholdChanged(round(it * PROGRESS_FACTOR).toInt()) },
            text = { Text(stringResource(R.string.battery_sync_charge_threshold_title)) }
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
        BatterySliderSetting(
            valueRange = 0.05f..BATTERY_LOW_MAX,
            value = lowThreshold / PROGRESS_FACTOR,
            onValueChanged = { onLowThresholdChanged(round(it * PROGRESS_FACTOR).toInt())},
            text = { Text(stringResource(R.string.battery_sync_low_threshold_title)) }
        )
    }
}
