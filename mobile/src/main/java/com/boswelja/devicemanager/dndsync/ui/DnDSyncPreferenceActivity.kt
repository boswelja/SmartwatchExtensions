package com.boswelja.devicemanager.dndsync.ui

import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.devicemanager.common.ui.AppTheme
import com.boswelja.devicemanager.common.ui.UpNavigationWatchPickerAppBar

class DnDSyncPreferenceActivity : AppCompatActivity() {

    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                val viewModel: DnDSyncPreferenceViewModel = viewModel()
                val registeredWatches by viewModel.watchManager.registeredWatches.observeAsState()
                val selectedWatch by viewModel.watchManager.selectedWatch.observeAsState()
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
                    if (LocalConfiguration.current.orientation == ORIENTATION_PORTRAIT) {
                        Column(Modifier.fillMaxSize()) {
                            DnDSyncSettingsHeader()
                            Divider()
                            DnDSyncPreferences()
                        }
                    } else {
                        Row(Modifier.fillMaxSize()) {
                            DnDSyncSettingsHeader()
                            DnDSyncPreferences()
                        }
                    }
                }
            }
        }
    }
}
