package com.boswelja.smartwatchextensions.proximity.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.ui.SwitchPreference
import com.boswelja.smartwatchextensions.common.ui.UpNavigationWatchPickerAppBar
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

class ProximitySettingsActivity : AppCompatActivity() {

    @ExperimentalCoroutinesApi
    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val watchManager = remember {
                WatchManager.getInstance(this)
            }
            val selectedWatch by watchManager.selectedWatch.collectAsState(null, Dispatchers.IO)
            val registeredWatches by watchManager.registeredWatches
                .collectAsState(emptyList(), Dispatchers.IO)

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

@ExperimentalMaterialApi
@ExperimentalCoroutinesApi
@Composable
fun ProximitySettingsScreen(modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val viewModel = viewModel<ProximitySettingsViewModel>()
    val phoneProximityNotiEnabled by viewModel.phoneProximityNotiSetting
        .collectAsState(false, Dispatchers.IO)
    val watchProximityNotiEnabled by viewModel.watchProximityNotiSetting
        .collectAsState(false, Dispatchers.IO)
    Column(modifier) {
        SwitchPreference(
            text = stringResource(R.string.proximity_phone_noti_title),
            secondaryText = stringResource(R.string.proximity_phone_noti_summary),
            isChecked = phoneProximityNotiEnabled,
            onCheckChanged = { isChecked ->
                scope.launch {
                    viewModel.setPhoneProximityNotiEnabled(isChecked)
                }
            }
        )
        SwitchPreference(
            text = stringResource(R.string.proximity_watch_noti_title),
            secondaryText = stringResource(R.string.proximity_watch_noti_summary),
            isChecked = watchProximityNotiEnabled,
            onCheckChanged = { isChecked ->
                scope.launch {
                    viewModel.setWatchProximityNotiEnabled(isChecked)
                }
            }
        )
    }
}
