package com.boswelja.smartwatchextensions.dashboard.ui

import androidx.compose.material3.SnackbarVisuals
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.boswelja.smartwatchextensions.appmanager.ui.appManagerNavigation
import com.boswelja.smartwatchextensions.batterysync.ui.batterySyncNavigation
import com.boswelja.smartwatchextensions.core.ui.snackbarVisuals
import com.boswelja.smartwatchextensions.dndsync.ui.dndSyncNavigation
import com.boswelja.smartwatchextensions.phonelocking.ui.phoneLockingNavigation
import com.boswelja.smartwatchextensions.proximity.ui.ProximitySettingsScreen

/**
 * All dashboard navigation destinations.
 * @param route The navigation route.
 */
enum class DashboardDestination(
    val route: String
) {
    APP_MANAGER("app-manager"),
    BATTERY_SYNC_SETTINGS("battery-sync-settings"),
    DND_SYNC_SETTINGS("dnd-sync-settings"),
    PHONE_LOCKING_SETTINGS("phone-locking-settings"),
    PROXIMITY_SETTINGS("proximity-settings")
}

/**
 * Adds all destinations used by [DashboardScreen] to the [NavGraphBuilder]. The entry point is
 * the route defined by [route].
 * @param modifier [Modifier].
 * @param contentPadding The padding for content.
 * @param navController [NavHostController].
 * @param route The entry point for this route.
 */
fun NavGraphBuilder.dashboardGraph(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp,
    navController: NavHostController,
    route: String,
    onShowSnackbar: suspend (SnackbarVisuals) -> Unit
) {
    composable(route) {
        DashboardScreen(
            modifier = modifier,
            contentPadding = contentPadding,
            onNavigateTo = { navController.navigate(it.route) }
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
    composable(DashboardDestination.PROXIMITY_SETTINGS.route) {
        ProximitySettingsScreen(
            modifier = modifier
        )
    }
}
