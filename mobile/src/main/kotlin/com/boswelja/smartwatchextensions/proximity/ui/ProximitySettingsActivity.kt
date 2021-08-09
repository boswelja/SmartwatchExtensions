package com.boswelja.smartwatchextensions.proximity.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.ui.AppTheme
import com.boswelja.smartwatchextensions.common.ui.Card
import com.boswelja.smartwatchextensions.common.ui.CardHeader
import com.boswelja.smartwatchextensions.common.ui.SwitchSetting
import com.boswelja.smartwatchextensions.common.ui.UpNavigationWatchPickerAppBar
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProximitySettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val watchManager = remember {
                WatchManager.getInstance(this)
            }
            val selectedWatch by watchManager.selectedWatch.collectAsState(null, Dispatchers.IO)
            val registeredWatches by watchManager.registeredWatches
                .collectAsState(emptyList(), Dispatchers.IO)

            AppTheme {
                Scaffold(
                    topBar = {
                        UpNavigationWatchPickerAppBar(
                            selectedWatch = selectedWatch,
                            watches = registeredWatches,
                            onWatchSelected = { watchManager.selectWatchById(it.id) },
                            onNavigateUp = { finish() }
                        )
                    }
                ) {
                    ProximitySettingsScreen(Modifier.padding(it))
                }
            }
        }
    }
}

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

@Composable
fun SeparationSettingsCard(modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val viewModel = viewModel<ProximitySettingsViewModel>()
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
