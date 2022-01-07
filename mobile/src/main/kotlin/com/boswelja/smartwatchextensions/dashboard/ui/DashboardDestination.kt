package com.boswelja.smartwatchextensions.dashboard.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.boswelja.smartwatchextensions.appmanager.ui.appManagerNavigation
import com.boswelja.smartwatchextensions.batterysync.ui.BatterySyncSettingsScreen
import com.boswelja.smartwatchextensions.dndsync.ui.DnDSyncSettingsScreen
import com.boswelja.smartwatchextensions.phonelocking.ui.PhoneLockingSettingsScreen
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
    onShowSnackbar: suspend (String) -> Unit
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
        onShowSnackbar = onShowSnackbar,
        onNavigate = { navController.navigate(it) },
        screenModifier = modifier
    )
    composable(DashboardDestination.BATTERY_SYNC_SETTINGS.route) {
        BatterySyncSettingsScreen(
            modifier = modifier.padding(vertical = contentPadding),
        )
    }
    composable(DashboardDestination.DND_SYNC_SETTINGS.route) {
        DnDSyncSettingsScreen(
            modifier = modifier,
            contentPadding = contentPadding
        )
    }
    composable(DashboardDestination.PHONE_LOCKING_SETTINGS.route) {
        PhoneLockingSettingsScreen(
            modifier = modifier,
            contentPadding = contentPadding
        )
    }
    composable(DashboardDestination.PROXIMITY_SETTINGS.route) {
        ProximitySettingsScreen(
            modifier = modifier,
            contentPadding = contentPadding
        )
    }
}
