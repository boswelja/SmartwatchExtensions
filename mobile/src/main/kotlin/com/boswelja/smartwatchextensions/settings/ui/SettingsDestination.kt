package com.boswelja.smartwatchextensions.settings.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.boswelja.smartwatchextensions.watchmanager.ui.WatchManagerScreen

enum class SettingsDestination(
    val route: String
) {
    WATCH_MANAGER("watch-manager")
}

/**
 * Adds all destinations used by [AppSettingsScreen] to the [NavGraphBuilder]. The entry point is
 * the route defined by [route].
 * @param modifier [Modifier].
 * @param contentPadding The padding for content.
 * @param navController [NavHostController].
 * @param route The entry point for this route.
 */
fun NavGraphBuilder.appSettingsGraph(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp,
    navController: NavHostController,
    route: String,
    onShowSnackbar: suspend (String) -> Unit
) {
    composable(route) {
        AppSettingsScreen(
            modifier = modifier,
            contentPadding = contentPadding,
            onNavigateTo = { navController.navigate(it.route) }
        )
    }
    composable(SettingsDestination.WATCH_MANAGER.route) {
        WatchManagerScreen(
            modifier = Modifier.padding(contentPadding),
            onShowSnackbar = onShowSnackbar
        )
    }
}
