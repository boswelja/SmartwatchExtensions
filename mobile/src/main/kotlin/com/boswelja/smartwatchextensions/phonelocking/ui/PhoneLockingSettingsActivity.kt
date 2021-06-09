package com.boswelja.smartwatchextensions.phonelocking.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.boswelja.smartwatchextensions.common.ui.AppTheme
import com.boswelja.smartwatchextensions.common.ui.UpNavigationWatchPickerAppBar
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi

class PhoneLockingSettingsActivity : AppCompatActivity() {

    @ExperimentalCoroutinesApi
    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val watchManager = remember { WatchManager.getInstance(this) }
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
                    PhoneLockingSettingsScreen()
                }
            }
        }
    }
}
