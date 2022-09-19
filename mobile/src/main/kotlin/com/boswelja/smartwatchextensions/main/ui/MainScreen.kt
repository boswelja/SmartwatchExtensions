package com.boswelja.smartwatchextensions.main.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.core.ui.theme.HarmonizedTheme
import com.boswelja.smartwatchextensions.dashboard.ui.dashboardGraph

private const val StartDestination = "dashboard"

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
    onShowSnackbar: suspend (SnackbarVisuals) -> Unit,
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = StartDestination
    ) {
        dashboardGraph(
            modifier = modifier,
            contentPadding = contentPadding,
            navController = navController,
            route = StartDestination,
            onShowSnackbar = onShowSnackbar
        )

        appBarGraph(
            onShowSnackbar = onShowSnackbar,
            onNavigateTo = {
                navController.navigate(it) {
                    launchSingleTop = true
                }
            },
            modifier = modifier,
            contentPadding = PaddingValues(contentPadding)
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

    HarmonizedTheme {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                val showUpButton = backStackEntry?.destination?.route != "dashboard"
                TopAppBar(
                    title = {
                        Text(stringResource(R.string.app_name))
                    },
                    canNavigateUp = showUpButton,
                    onNavigateUp = navController::navigateUp,
                    onNavigateTo = {
                        navController.navigate(it) {
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
