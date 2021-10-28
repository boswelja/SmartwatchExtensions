package com.boswelja.smartwatchextensions.devicemanagement.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.boswelja.smartwatchextensions.devicemanagement.ui.register.RegisterWatchScreen

/**
 * All navigation destinations for Watch Manager.
 * @param route The navigation route.
 */
enum class WatchManagerDestination(val route: String) {
    REGISTER_WATCHES("register-watches")
}

/**
 * Adds all destinations used by [WatchManagerScreen] to the [NavGraphBuilder]. The entry point is
 * the route defined by [route].
 * @param modifier [Modifier].
 * @param contentPadding The padding for content.
 * @param navController [NavHostController].
 * @param route The entry point for this route.
 */
fun NavGraphBuilder.watchManagerGraph(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp,
    navController: NavHostController,
    route: String,
    onShowSnackbar: suspend (String) -> Unit
) {
    composable(route) {
        WatchManagerScreen(
            modifier = modifier,
            contentPadding = contentPadding,
            onShowSnackbar = onShowSnackbar,
            onNavigateTo = { navController.navigate(it.route) }
        )
    }
    composable(WatchManagerDestination.REGISTER_WATCHES.route) {
        RegisterWatchScreen(
            modifier = modifier,
            contentPadding = contentPadding
        )
    }
}
