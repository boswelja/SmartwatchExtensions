package com.boswelja.smartwatchextensions.extensions.ui

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.ChipDefaults.secondaryChipColors
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.about.ui.AboutActivity
import com.boswelja.smartwatchextensions.batterysync.ui.BatteryStatsCard
import com.boswelja.smartwatchextensions.common.InsetDefaults.RoundScreenInset
import com.boswelja.smartwatchextensions.common.rotaryInput
import com.boswelja.smartwatchextensions.common.roundScreenPadding
import com.boswelja.smartwatchextensions.common.startActivity
import com.boswelja.smartwatchextensions.phonelocking.ui.PhoneLockingCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ExtensionsScreen() {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .padding(8.dp)
            .roundScreenPadding(PaddingValues(vertical = RoundScreenInset))
            .rotaryInput { delta ->
                coroutineScope.launch {
                    scrollState.scrollBy(delta)
                }
            },
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Extensions(Modifier.fillMaxWidth())
        Links(Modifier.fillMaxWidth())
    }
}

@Composable
fun Extensions(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val viewModel: ExtensionsViewModel = viewModel()
        val batterySyncEnabled by viewModel.batterySyncEnabled.collectAsState(false, Dispatchers.IO)
        val phoneLockingEnabled by viewModel.phoneLockingEnabled
            .collectAsState(false, Dispatchers.IO)
        val batteryPercent by viewModel.batteryPercent.collectAsState(0, Dispatchers.IO)
        val phoneName by viewModel.phoneName
            .collectAsState(stringResource(R.string.default_phone_name), Dispatchers.IO)

        val cardModifier = Modifier.fillMaxWidth()
        BatteryStatsCard(
            modifier = cardModifier,
            enabled = batterySyncEnabled,
            percent = batteryPercent,
            phoneName = phoneName
        ) {
            viewModel.updateBatteryStats()
        }
        PhoneLockingCard(
            modifier = cardModifier,
            enabled = phoneLockingEnabled,
            phoneName = phoneName
        ) {
            viewModel.requestLockPhone()
        }
    }
}

@Composable
fun Links(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Chip(
            colors = secondaryChipColors(),
            label = {
                Text(stringResource(R.string.about_app_title))
            },
            icon = {
                Icon(
                    modifier = Modifier.size(ChipDefaults.IconSize),
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null
                )
            },
            onClick = {
                context.startActivity<AboutActivity>()
            }
        )
    }
}
