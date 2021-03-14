package com.boswelja.devicemanager.watchmanager.ui.register

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Done
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.res.stringResource
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.LifecycleAwareTimer
import com.boswelja.devicemanager.common.ui.UpNavigationAppBar

class RegisterWatchActivity : AppCompatActivity() {

    private val viewModel: RegisterWatchViewModel by viewModels()
    private val availableWatchUpdateTimer = LifecycleAwareTimer(TIMER_UPDATE_SECONDS) {
        viewModel.refreshData()
    }

    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                val registeredWatches by viewModel.registeredWatches.observeAsState()
                Scaffold(
                    topBar = { UpNavigationAppBar(onNavigateUp = { finish() }) },
                    floatingActionButton = {
                        ExtendedFloatingActionButton(
                            text = { Text(stringResource(R.string.button_finish)) },
                            icon = { Icon(Icons.Outlined.Done, null) },
                            onClick = { finish() }
                        )
                    }
                ) {
                    RegisterWatchesScreen(registeredWatches = registeredWatches)
                }
            }
        }

        lifecycle.addObserver(availableWatchUpdateTimer)

        viewModel.availableWatches.observe(this) {
            it.forEach { watch ->
                viewModel.registerWatch(watch)
            }
        }
    }

    companion object {
        private const val TIMER_UPDATE_SECONDS: Long = 5
    }
}
