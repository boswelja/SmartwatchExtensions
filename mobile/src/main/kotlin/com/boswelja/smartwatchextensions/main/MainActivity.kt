package com.boswelja.smartwatchextensions.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.lifecycleScope
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.aboutapp.ui.AboutAppScreen
import com.boswelja.smartwatchextensions.appsettings.ui.AppSettingsScreen
import com.boswelja.smartwatchextensions.common.ui.AppTheme
import com.boswelja.smartwatchextensions.common.ui.WatchPickerAppBar
import com.boswelja.smartwatchextensions.dashboard.ui.DashboardScreen
import com.boswelja.smartwatchextensions.messages.ui.MessagesScreen
import com.boswelja.smartwatchextensions.onboarding.ui.OnboardingActivity
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    @ExperimentalCoroutinesApi
    private val viewModel: MainViewModel by viewModels()

    @ExperimentalFoundationApi
    @ExperimentalCoroutinesApi
    @ExperimentalAnimationApi
    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent?.hasExtra(EXTRA_WATCH_ID) == true) {
            viewModel.selectWatchById(UUID.fromString(intent.getStringExtra(EXTRA_WATCH_ID)))
        }

        setContent {
            val selectedWatch by viewModel.selectedWatch.collectAsState(null, Dispatchers.IO)
            val registeredWatches by viewModel.registeredWatches
                .collectAsState(emptyList(), Dispatchers.IO)

            var currentDestination by rememberSaveable { mutableStateOf(Destination.DASHBOARD) }

            val scaffoldState = rememberScaffoldState()

            // Switch the destination back to Dashboard if we're not already there
            BackHandler(enabled = currentDestination != Destination.DASHBOARD) {
                currentDestination = Destination.DASHBOARD
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
                        currentDestination = currentDestination
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

enum class Destination {
    DASHBOARD,
    MESSAGES,
    SETTINGS,
    ABOUT
}

@ExperimentalFoundationApi
@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@Composable
fun MainScreen(scaffoldState: ScaffoldState, currentDestination: Destination) {
    Crossfade(targetState = currentDestination) {
        when (it) {
            Destination.DASHBOARD -> DashboardScreen(Modifier.fillMaxSize())
            Destination.MESSAGES -> MessagesScreen(scaffoldState = scaffoldState)
            Destination.SETTINGS -> AppSettingsScreen()
            Destination.ABOUT -> AboutAppScreen()
        }
    }
}

@Composable
fun BottonNav(
    currentDestination: Destination,
    setCurrentDestination: (Destination) -> Unit
) {
    BottomNavigation(
        backgroundColor = MaterialTheme.colors.background
    ) {
        BottomNavItem(
            selected = currentDestination == Destination.DASHBOARD,
            icon = Icons.Outlined.Dashboard,
            label = stringResource(R.string.bottom_nav_dashboard_label),
            onClick = {
                setCurrentDestination(Destination.DASHBOARD)
            }
        )
        BottomNavItem(
            selected = currentDestination == Destination.MESSAGES,
            icon = Icons.Outlined.Message,
            label = stringResource(R.string.nav_messages_label),
            onClick = {
                setCurrentDestination(Destination.MESSAGES)
            }
        )
        BottomNavItem(
            selected = currentDestination == Destination.SETTINGS,
            icon = Icons.Outlined.Settings,
            label = stringResource(R.string.bottom_nav_app_settings_label),
            onClick = {
                setCurrentDestination(Destination.SETTINGS)
            }
        )
        BottomNavItem(
            selected = currentDestination == Destination.ABOUT,
            icon = Icons.Outlined.Info,
            label = stringResource(R.string.bottom_nav_about_label),
            onClick = {
                setCurrentDestination(Destination.ABOUT)
            }
        )
    }
}

@Composable
fun RowScope.BottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    BottomNavigationItem(
        selected = selected,
        icon = { Icon(icon, null) },
        label = { Text(label) },
        onClick = onClick,
        alwaysShowLabel = false,
        selectedContentColor = MaterialTheme.colors.primary,
        unselectedContentColor = MaterialTheme.colors.onSurface
            .copy(alpha = ContentAlpha.medium)
    )
}
