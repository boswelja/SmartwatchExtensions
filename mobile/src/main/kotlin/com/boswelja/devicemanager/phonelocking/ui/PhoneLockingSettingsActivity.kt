package com.boswelja.devicemanager.phonelocking.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import com.boswelja.devicemanager.common.ui.AppTheme
import com.boswelja.devicemanager.common.ui.UpNavigationWatchPickerAppBar
import com.boswelja.devicemanager.watchmanager.WatchManager

class PhoneLockingSettingsActivity : AppCompatActivity() {

    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val watchManager = remember { WatchManager.getInstance(this) }
            val selectedWatch by watchManager.selectedWatch.observeAsState()
            val registeredWatches by watchManager.registeredWatches.observeAsState()
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
                    PhoneLockingSettingsScreen()
                }
            }
        }
    }
}
