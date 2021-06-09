package com.boswelja.smartwatchextensions.dndsync.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.smartwatchextensions.common.ui.AppTheme
import com.boswelja.smartwatchextensions.common.ui.UpNavigationWatchPickerAppBar
import kotlinx.coroutines.ExperimentalCoroutinesApi

class DnDSyncSettingsActivity : AppCompatActivity() {

    @ExperimentalCoroutinesApi
    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                val viewModel: DnDSyncSettingsViewModel = viewModel()
                val registeredWatches by viewModel.watchManager.registeredWatches.observeAsState()
                val selectedWatch by viewModel.watchManager.selectedWatchLiveData.observeAsState()
                Scaffold(
                    topBar = {
                        UpNavigationWatchPickerAppBar(
                            onNavigateUp = { finish() },
                            watches = registeredWatches,
                            selectedWatch = selectedWatch,
                            onWatchSelected = { viewModel.watchManager.selectWatchById(it.id) }
                        )
                    }
                ) {
                    DnDSyncSettingsScreen()
                }
            }
        }
    }
}
