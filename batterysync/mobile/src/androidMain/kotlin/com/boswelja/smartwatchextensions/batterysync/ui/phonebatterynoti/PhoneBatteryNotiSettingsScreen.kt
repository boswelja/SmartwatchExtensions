package com.boswelja.smartwatchextensions.batterysync.ui.phonebatterynoti

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.boswelja.smartwatchextensions.batterysync.R
import com.boswelja.smartwatchextensions.settings.ui.SwitchSetting
import org.koin.androidx.compose.getViewModel

/**
 * A Composable screen to let the user control notifications related to phone battery.
 * @param modifier [Modifier].
 */
@Composable
fun PhoneBatteryNotiSettingsScreen(
    modifier: Modifier = Modifier
) {
    val viewModel: PhoneBatteryNotiSettingsViewModel = getViewModel()

    val phoneChargeNotiEnabled by viewModel.phoneChargeNotiEnabled.collectAsState()
    val phoneLowNotiEnabled by viewModel.phoneLowNotiEnabled.collectAsState()

    val chargeThreshold by viewModel.chargeThreshold.collectAsState()
    val lowThreshold by viewModel.batteryLowThreshold.collectAsState()

    Column(modifier) {
        SwitchSetting(
            label = { Text(stringResource(R.string.battery_sync_phone_charge_noti_title)) },
            summary = {
                val text = stringResource(
                    R.string.battery_sync_phone_charge_noti_summary,
                    chargeThreshold
                )
                Text(text)
            },
            checked = phoneChargeNotiEnabled,
            onCheckChanged = viewModel::setPhoneChargeNotiEnabled
        )
        SwitchSetting(
            label = { Text(stringResource(R.string.battery_sync_phone_low_noti_title)) },
            summary = {
                val text = stringResource(R.string.battery_sync_phone_low_noti_summary, lowThreshold)
                Text(text)
            },
            checked = phoneLowNotiEnabled,
            onCheckChanged = viewModel::setPhoneLowNotiEnabled
        )
    }
}
