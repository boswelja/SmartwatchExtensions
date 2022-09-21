package com.boswelja.smartwatchextensions.settings.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.boswelja.smartwatchextensions.settings.ui.register.RegisterWatchScreen

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
 * @param route The entry point for this route.
 */
fun NavGraphBuilder.watchManagerGraph(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
    onNavigateTo: (String) -> Unit,
    route: String,
    onShowSnackbar: suspend (SnackbarVisuals) -> Unit
) {
    composable(route) {
        WatchManagerScreen(
            modifier = modifier,
            contentPadding = contentPadding,
            onShowSnackbar = onShowSnackbar,
            onNavigateTo = { onNavigateTo(it.route) }
        )
    }
    composable(WatchManagerDestination.REGISTER_WATCHES.route) {
        RegisterWatchScreen(
            modifier = modifier,
            contentPadding = contentPadding
        )
    }
}
