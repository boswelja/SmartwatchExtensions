package com.boswelja.smartwatchextensions.batterysync.widget.config

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.ui.AppTheme
import com.boswelja.smartwatchextensions.common.ui.BaseWidgetConfigActivity
import com.boswelja.smartwatchextensions.common.ui.UpNavigationAppBar
import com.boswelja.smartwatchextensions.common.ui.WatchWidgetConfigScreen
import com.boswelja.watchconnection.core.Watch
import kotlinx.coroutines.Dispatchers

class BatteryWidgetConfigActivity : BaseWidgetConfigActivity() {

    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                val (selectedWatch, onWatchSelected) = remember { mutableStateOf<Watch?>(null) }
                val viewModel: BatteryWidgetConfigViewModel = viewModel()
                val registeredWatches by viewModel.registeredWatches.collectAsState(
                    emptyList(),
                    Dispatchers.IO
                )

                Scaffold(
                    topBar = {
                        UpNavigationAppBar(onNavigateUp = { finish() })
                    },
                    floatingActionButton = {
                        ExtendedFloatingActionButton(
                            text = { Text(stringResource(R.string.button_finish)) },
                            icon = { Icon(Icons.Outlined.Check, null) },
                            onClick = {
                                selectedWatch?.let { finishWidgetConfig(it) } ?: finish()
                            }
                        )
                    }
                ) {
                    WatchWidgetConfigScreen(
                        modifier = Modifier.fillMaxSize(),
                        availableWatches = registeredWatches,
                        selectedWatch = selectedWatch,
                        onWatchSelected = onWatchSelected
                    )
                }
            }
        }
    }
}
