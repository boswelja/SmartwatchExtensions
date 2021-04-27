package com.boswelja.smartwatchextensions.batterysync.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import com.boswelja.smartwatchextensions.common.ui.AppTheme
import com.boswelja.smartwatchextensions.common.ui.UpNavigationWatchPickerAppBar
import com.boswelja.smartwatchextensions.watchmanager.WatchManager

class BatterySyncSettingsActivity : AppCompatActivity() {

    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                val watchManager = remember {
                    WatchManager.getInstance(this)
                }
                val selectedWatch by watchManager.selectedWatch.observeAsState()
                val registeredWatches by watchManager.registeredWatches.observeAsState()

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
                    Column {
                        BatterySyncSettingsHeader()
                        Divider()
                        BatterySyncSettingsScreen()
                    }
                }
            }
        }
    }
}
