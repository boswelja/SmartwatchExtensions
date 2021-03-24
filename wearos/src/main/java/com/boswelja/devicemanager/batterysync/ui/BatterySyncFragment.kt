package com.boswelja.devicemanager.batterysync.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.AppTheme
import com.boswelja.devicemanager.common.BatteryIcon

class BatterySyncFragment : Fragment() {

    @ExperimentalMaterialApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                AppTheme {
                    val viewModel: BatterySyncViewModel = viewModel()
                    val batterySyncEnabled by viewModel.batterySyncEnabled.observeAsState()
                    val batteryPercent by viewModel.batteryPercent.observeAsState()
                    val phoneName by viewModel.phoneName.observeAsState()
                    BatterySyncScreen(
                        batterySyncEnabled == true,
                        batteryPercent ?: 0,
                        phoneName ?: stringResource(R.string.default_phone_name)
                    ) {
                        viewModel.updateBatteryStats()
                    }
                }
            }
        }
    }
}

@ExperimentalMaterialApi
@Composable
fun BatterySyncScreen(
    batterySyncEnabled: Boolean,
    batteryPercent: Int,
    phoneName: String,
    onClick: () -> Unit
) {
    if (batterySyncEnabled) {
        BatteryStatus(
            percent = batteryPercent,
            phoneName = phoneName,
            onClick = onClick
        )
    } else {
        BatterySyncDisabled()
    }
}

@ExperimentalMaterialApi
@Composable
fun BatterySyncDisabled() {
    ListItem(
        text = { Text(stringResource(R.string.battery_sync_disabled)) },
        icon = { BatteryIcon(percent = -1) }
    )
}

@Composable
fun BatteryStatus(
    percent: Int,
    phoneName: String,
    onClick: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BatteryIcon(percent, modifier = Modifier.size(56.dp))
        Text(
            stringResource(R.string.battery_sync_hint_text, phoneName),
            style = MaterialTheme.typography.body1
        )
        Text(
            stringResource(R.string.battery_percent, percent.toString()),
            style = MaterialTheme.typography.h6
        )
    }
}
