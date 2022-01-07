package com.boswelja.smartwatchextensions.batterysync.ui

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation

fun NavGraphBuilder.batterySyncNavigation(
    graphRoute: String,
    onShowSnackbar: suspend (String) -> Unit,
    onNavigate: (route: String) -> Unit,
    screenModifier: Modifier = Modifier
) = navigation(
    BatterySyncDestinations.BATTERY_SYNC_SETTINGS.route,
    graphRoute
) {
    composable(BatterySyncDestinations.BATTERY_SYNC_SETTINGS.route) {
        BatterySyncSettingsScreen(screenModifier)
    }
}

internal enum class BatterySyncDestinations(val route: String) {
    BATTERY_SYNC_SETTINGS("battery_sync_settings")
}
