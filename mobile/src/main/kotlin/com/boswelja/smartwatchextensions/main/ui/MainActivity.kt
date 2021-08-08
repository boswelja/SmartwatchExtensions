package com.boswelja.smartwatchextensions.main.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.boswelja.smartwatchextensions.aboutapp.ui.AboutAppScreen
import com.boswelja.smartwatchextensions.common.ui.AppTheme
import com.boswelja.smartwatchextensions.common.ui.WatchPickerAppBar
import com.boswelja.smartwatchextensions.dashboard.ui.DashboardScreen
import com.boswelja.smartwatchextensions.messages.ui.MessagesScreen
import com.boswelja.smartwatchextensions.onboarding.ui.OnboardingActivity
import com.boswelja.smartwatchextensions.settings.ui.AppSettingsScreen
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent?.hasExtra(EXTRA_WATCH_ID) == true) {
            viewModel.selectWatchById(UUID.fromString(intent.getStringExtra(EXTRA_WATCH_ID)))
        }

        setContent {
            val selectedWatch by viewModel.selectedWatch.collectAsState(null, Dispatchers.IO)
            val registeredWatches by viewModel.registeredWatches
                .collectAsState(emptyList(), Dispatchers.IO)

            var currentDestination by rememberSaveable {
                mutableStateOf(BottomNavDestination.DASHBOARD)
            }

            val scaffoldState = rememberScaffoldState()

            // Switch the destination back to Dashboard if we're not already there
            BackHandler(enabled = currentDestination != BottomNavDestination.DASHBOARD) {
                currentDestination = BottomNavDestination.DASHBOARD
            }

            AppTheme {
                Scaffold(
                    scaffoldState = scaffoldState,
                    topBar = {
                        WatchPickerAppBar(
                            selectedWatch = selectedWatch,
                            watches = registeredWatches,
                            onWatchSelected = { viewModel.selectWatchById(it.id) }
                        )
                    },
                    bottomBar = {
                        BottonNav(currentDestination) { currentDestination = it }
                    }
                ) {
                    MainScreen(
                        scaffoldState = scaffoldState,
                        currentBottomNavDestination = currentDestination
                    )
                }
            }
        }

        lifecycleScope.launch {
            viewModel.needsSetup.collect {
                if (it) {
                    startActivity(
                        Intent(this@MainActivity, OnboardingActivity::class.java)
                    )
                    finish()
                }
            }
        }
    }

    companion object {
        const val EXTRA_WATCH_ID = "extra_watch_id"
    }
}

@Composable
fun MainScreen(scaffoldState: ScaffoldState, currentBottomNavDestination: BottomNavDestination) {
    Crossfade(targetState = currentBottomNavDestination) {
        when (it) {
            BottomNavDestination.DASHBOARD -> DashboardScreen(Modifier.fillMaxSize())
            BottomNavDestination.MESSAGES -> MessagesScreen(scaffoldState = scaffoldState)
            BottomNavDestination.SETTINGS -> AppSettingsScreen()
            BottomNavDestination.ABOUT -> AboutAppScreen()
        }
    }
}
