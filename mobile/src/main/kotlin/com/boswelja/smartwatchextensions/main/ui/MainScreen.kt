package com.boswelja.smartwatchextensions.main.ui

import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.aboutapp.ui.AboutAppScreen
import com.boswelja.smartwatchextensions.common.ui.TopAppBar
import com.boswelja.smartwatchextensions.core.ui.theme.HarmonizedTheme
import com.boswelja.smartwatchextensions.dashboard.ui.dashboardGraph
import com.boswelja.smartwatchextensions.settings.ui.appSettingsGraph

/**
 * A Composable screen for displaying the main content.
 * @param modifier [Modifier].
 * @param contentPadding The padding around the screen.
 * @param onShowSnackbar Called when a snackbar should be displayed.
 * @param navController [NavHostController].
 */
@Composable
fun MainNavHost(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp,
    onShowSnackbar: suspend (String) -> Unit,
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
            onShowSnackbar = onShowSnackbar
        )

        // Load settings
        appSettingsGraph(
            modifier = modifier,
            contentPadding = contentPadding,
            navController = navController,
            route = BottomNavDestination.SETTINGS.route,
            onShowSnackbar = onShowSnackbar
        )
    }
}

/**
 * This screen lays out the main app content.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val decayAnimationSpec = rememberSplineBasedDecay<Float>()
    val scrollBehavior = remember(decayAnimationSpec) {
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(decayAnimationSpec)
    }

    HarmonizedTheme {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                val showUpButton = BottomNavDestination.values()
                    .none { it.route == backStackEntry?.destination?.route }
                TopAppBar(
                    title = {
                        Text(stringResource(R.string.app_name))
                    },
                    canNavigateUp = showUpButton,
                    onNavigateUp = navController::navigateUp,
                    scrollBehavior = scrollBehavior
                )
            },
            bottomBar = {
                BottomNavBar(
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
            MainNavHost(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it),
                onShowSnackbar = { message ->
                    snackbarHostState.showSnackbar(message)
                },
                navController = navController
            )
        }
    }
}
