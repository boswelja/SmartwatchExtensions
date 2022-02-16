package com.boswelja.smartwatchextensions.main.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.boswelja.smartwatchextensions.aboutapp.ui.AboutAppScreen
import com.boswelja.smartwatchextensions.common.ui.AppTheme
import com.boswelja.smartwatchextensions.common.ui.UpNavigationWatchPickerAppBar
import com.boswelja.smartwatchextensions.common.ui.WatchPickerAppBar
import com.boswelja.smartwatchextensions.dashboard.ui.dashboardGraph
import com.boswelja.smartwatchextensions.onboarding.ui.OnboardingActivity
import com.boswelja.smartwatchextensions.settings.ui.appSettingsGraph
import com.boswelja.watchconnection.common.Watch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * The main app entry point.
 */
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()

        if (intent?.hasExtra(EXTRA_WATCH_ID) == true) {
            viewModel.selectWatchById(intent.getStringExtra(EXTRA_WATCH_ID)!!)
        }

        setContent {
            val selectedWatch by viewModel.selectedWatch.collectAsState(null, Dispatchers.IO)
            val registeredWatches by viewModel.registeredWatches
                .collectAsState(emptyList(), Dispatchers.IO)

            val navController = rememberNavController()
            val backStackEntry by navController.currentBackStackEntryAsState()
            val scaffoldState = rememberScaffoldState()

            AppTheme {
                Scaffold(
                    scaffoldState = scaffoldState,
                    topBar = {
                        val showUpButton = BottomNavDestination.values()
                            .none { it.route == backStackEntry?.destination?.route }
                        MainAppBar(
                            showUpButton = showUpButton,
                            selectedWatch = selectedWatch,
                            registeredWatches = registeredWatches,
                            onWatchSelected = viewModel::selectWatchById,
                            onNavigateUp = navController::navigateUp
                        )
                    },
                    bottomBar = {
                        BottonNav(
                            currentDestination = backStackEntry?.destination,
                            onNavigateTo = {
                                navController.navigate(it.route) {
                                    popUpTo(BottomNavDestination.DASHBOARD.route)
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                ) {
                    MainScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(it),
                        scaffoldState = scaffoldState,
                        navController = navController
                    )
                }
            }
        }

        lifecycleScope.launch {
            viewModel.needsSetup.collect {
                if (it) {
                    startActivity(Intent(this@MainActivity, OnboardingActivity::class.java))
                    finish()
                }
            }
        }
    }

    companion object {

        /**
         * The watch ID to select on launch.
         */
        const val EXTRA_WATCH_ID = "extra_watch_id"
    }
}

/**
 * A Composable to display the main app bar.
 * @param showUpButton Whether a button to navigate up should be shown.
 * @param selectedWatch The currently selected watch.
 * @param registeredWatches Currently registered watches.
 * @param onWatchSelected Called when a new watch is selected.
 * @param onNavigateUp Called when up navigation is requested.
 */
@Composable
fun MainAppBar(
    showUpButton: Boolean,
    selectedWatch: Watch?,
    registeredWatches: List<Watch>,
    onWatchSelected: (String) -> Unit,
    onNavigateUp: () -> Unit
) {
    if (showUpButton) {
        UpNavigationWatchPickerAppBar(
            selectedWatch = selectedWatch,
            watches = registeredWatches,
            onWatchSelected = { onWatchSelected(it.uid) },
            onNavigateUp = onNavigateUp
        )
    } else {
        WatchPickerAppBar(
            selectedWatch = selectedWatch,
            watches = registeredWatches,
            onWatchSelected = { onWatchSelected(it.uid) }
        )
    }
}

/**
 * A Composable screen for displaying the main content.
 * @param modifier [Modifier].
 * @param contentPadding The padding around the screen.
 * @param scaffoldState [ScaffoldState].
 * @param navController [NavHostController].
 */
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp,
    scaffoldState: ScaffoldState,
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = BottomNavDestination.DASHBOARD.route
    ) {
        // Bottom nav destinations
        composable(BottomNavDestination.ABOUT.route) {
            AboutAppScreen(
                modifier = modifier,
                contentPadding = contentPadding
            )
        }

        // Dashboard destinations
        dashboardGraph(
            modifier = modifier,
            contentPadding = contentPadding,
            navController = navController,
            route = BottomNavDestination.DASHBOARD.route,
            onShowSnackbar = {
                scaffoldState.snackbarHostState.showSnackbar(it)
            }
        )

        // Load settings
        appSettingsGraph(
            modifier = modifier,
            contentPadding = contentPadding,
            navController = navController,
            route = BottomNavDestination.SETTINGS.route,
            onShowSnackbar = {
                scaffoldState.snackbarHostState.showSnackbar(it)
            }
        )
    }
}
