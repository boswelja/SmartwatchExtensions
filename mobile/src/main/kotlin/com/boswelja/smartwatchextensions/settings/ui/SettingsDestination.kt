package com.boswelja.smartwatchextensions.settings.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.boswelja.smartwatchextensions.devicemanagement.ui.watchManagerGraph

/**
 * All destinations reachable from withing App Settings.
 * @param route The navigation route for the destination.
 */
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
 * @param route The entry point for this route.
 */
fun NavGraphBuilder.appSettingsGraph(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
    onNavigateTo: (String) -> Unit,
    route: String,
    onShowSnackbar: suspend (String) -> Unit
) {
    composable(route) {
        AppSettingsScreen(
            modifier = modifier,
            contentPadding = contentPadding,
            onNavigateTo = { onNavigateTo(it.route) }
        )
    }
    watchManagerGraph(
        modifier = modifier,
        contentPadding = contentPadding,
        onNavigateTo = onNavigateTo,
        route = SettingsDestination.WATCH_MANAGER.route,
        onShowSnackbar = onShowSnackbar
    )
}
