package com.boswelja.smartwatchextensions.proximity.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.ui.Card
import com.boswelja.smartwatchextensions.common.ui.CardHeader
import com.boswelja.smartwatchextensions.settings.ui.SwitchSetting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

/**
 * A Composable screen for displaying proximity settings.
 * @param modifier [Modifier].
 * @param contentPadding The screen padding.
 */
@Composable
fun ProximitySettingsScreen(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp
) {
    Column(
        modifier = modifier.padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(contentPadding)
    ) {
        SeparationSettingsCard(Modifier.fillMaxWidth())
    }
}

/**
 * A Composable for displaying separation settings.
 * @param modifier [Modifier].
 */
@Composable
fun SeparationSettingsCard(modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val viewModel = getViewModel<ProximitySettingsViewModel>()
    val phoneProximityNotiEnabled by viewModel.phoneProximityNotiSetting
        .collectAsState(false, Dispatchers.IO)
    val watchProximityNotiEnabled by viewModel.watchProximityNotiSetting
        .collectAsState(false, Dispatchers.IO)

    Card(
        modifier = modifier,
        header = {
            CardHeader(
                title = {
                    Text(stringResource(R.string.separation_alerts_title))
                }
            )
        }
    ) {
        Column {
            SwitchSetting(
                label = { Text(stringResource(R.string.proximity_phone_noti_title)) },
                summary = { Text(stringResource(R.string.proximity_phone_noti_summary)) },
                checked = phoneProximityNotiEnabled,
                onCheckChanged = { isChecked ->
                    scope.launch {
                        viewModel.setPhoneProximityNotiEnabled(isChecked)
                    }
                }
            )
            SwitchSetting(
                label = { Text(stringResource(R.string.proximity_watch_noti_title)) },
                summary = { Text(stringResource(R.string.proximity_watch_noti_summary)) },
                checked = watchProximityNotiEnabled,
                onCheckChanged = { isChecked ->
                    scope.launch {
                        viewModel.setWatchProximityNotiEnabled(isChecked)
                    }
                }
            )
        }
    }
}
