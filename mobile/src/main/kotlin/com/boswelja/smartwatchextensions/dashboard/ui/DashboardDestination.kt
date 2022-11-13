package com.boswelja.smartwatchextensions.dashboard.ui

import androidx.compose.material3.SnackbarVisuals
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.boswelja.smartwatchextensions.appmanager.ui.appManagerNavigation
import com.boswelja.smartwatchextensions.batterysync.ui.batterySyncNavigation
import com.boswelja.smartwatchextensions.core.ui.snackbarVisuals
import com.boswelja.smartwatchextensions.dndsync.ui.dndSyncNavigation
import com.boswelja.smartwatchextensions.phonelocking.ui.phoneLockingNavigation
import com.boswelja.smartwatchextensions.watchmanager.ui.register.RegisterWatchScreen

/**
 * All dashboard navigation destinations.
 * @param route The navigation route.
 */
enum class DashboardDestination(
    val route: String
) {
    REGISTER_WATCH("register-watch"),
    APP_MANAGER("app-manager"),
    BATTERY_SYNC_SETTINGS("battery-sync-settings"),
    DND_SYNC_SETTINGS("dnd-sync-settings"),
    PHONE_LOCKING_SETTINGS("phone-locking-settings"),
}

/**
 * Adds all destinations used by [DashboardScreen] to the [NavGraphBuilder]. The entry point is
 * the route defined by [route].
 * @param modifier [Modifier].
 * @param navController [NavHostController].
 * @param route The entry point for this route.
 */
fun NavGraphBuilder.dashboardGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    route: String,
    onShowSnackbar: suspend (SnackbarVisuals) -> Unit
) {
    composable(route) {
        DashboardScreen(
            modifier = modifier,
            onNavigateTo = { navController.navigate(it.route) }
        )
    }
    composable(DashboardDestination.REGISTER_WATCH.route) {
        RegisterWatchScreen(
            onRegistrationFinished = { navController.popBackStack() },
            modifier = modifier
        )
    }
    appManagerNavigation(
        DashboardDestination.APP_MANAGER.route,
        onShowSnackbar = { onShowSnackbar(snackbarVisuals(message = it)) },
        onNavigate = { navController.navigate(it) },
        screenModifier = modifier
    )
    batterySyncNavigation(
        DashboardDestination.BATTERY_SYNC_SETTINGS.route,
        onNavigate = { navController.navigate(it) },
        screenModifier = modifier
    )
    phoneLockingNavigation(
        DashboardDestination.PHONE_LOCKING_SETTINGS.route,
        onNavigate = { navController.navigate(it) },
        screenModifier = modifier
    )
    dndSyncNavigation(
        DashboardDestination.DND_SYNC_SETTINGS.route,
        screenModifier = modifier
    )
}
