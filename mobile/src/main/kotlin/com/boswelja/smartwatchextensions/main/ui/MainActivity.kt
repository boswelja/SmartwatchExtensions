package com.boswelja.smartwatchextensions.main.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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

            val navController = rememberNavController()
            val backStackEntry by navController.currentBackStackEntryAsState()
            val scaffoldState = rememberScaffoldState()

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
    scaffoldState: ScaffoldState,
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = BottomNavDestination.DASHBOARD.route
    ) {
        composable(BottomNavDestination.DASHBOARD.route) {
            DashboardScreen(modifier)
        }
        composable(BottomNavDestination.MESSAGES.route) {
            MessagesScreen(scaffoldState = scaffoldState)
        }
        composable(BottomNavDestination.SETTINGS.route) {
            AppSettingsScreen()
        }
        composable(BottomNavDestination.ABOUT.route) {
            AboutAppScreen()
        }
    }
}
