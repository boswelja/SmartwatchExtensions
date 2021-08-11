package com.boswelja.smartwatchextensions.main.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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
import com.boswelja.smartwatchextensions.messages.ui.MessageDestination
import com.boswelja.smartwatchextensions.messages.ui.MessageHistoryScreen
import com.boswelja.smartwatchextensions.messages.ui.MessagesScreen
import com.boswelja.smartwatchextensions.onboarding.ui.OnboardingActivity
import com.boswelja.smartwatchextensions.settings.ui.appSettingsGraph
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

            val navController = rememberNavController()
            val backStackEntry by navController.currentBackStackEntryAsState()
            val scaffoldState = rememberScaffoldState()

            AppTheme {
                Scaffold(
                    scaffoldState = scaffoldState,
                    topBar = {
                        val showUpButton = BottomNavDestination.values()
                            .none { it.route == backStackEntry?.destination?.route }
                        if (showUpButton) {
                            UpNavigationWatchPickerAppBar(
                                selectedWatch = selectedWatch,
                                watches = registeredWatches,
                                onWatchSelected = { viewModel.selectWatchById(it.id) },
                                onNavigateUp = { navController.popBackStack() }
                            )
                        } else {
                            WatchPickerAppBar(
                                selectedWatch = selectedWatch,
                                watches = registeredWatches,
                                onWatchSelected = { viewModel.selectWatchById(it.id) }
                            )
                        }
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
        composable(BottomNavDestination.MESSAGES.route) {
            MessagesScreen(
                modifier = modifier,
                contentPadding = contentPadding,
                scaffoldState = scaffoldState,
                onNavigateTo = { navController.navigate(it.route) }
            )
        }
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

        // Message destinations
        composable(MessageDestination.MessageHistory.route) {
            MessageHistoryScreen(
                modifier = modifier.padding(contentPadding),
                onShowSnackbar = {
                    scaffoldState.snackbarHostState.showSnackbar(it)
                }
            )
        }

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
