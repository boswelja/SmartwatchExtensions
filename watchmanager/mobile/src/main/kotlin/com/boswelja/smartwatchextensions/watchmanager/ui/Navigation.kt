package com.boswelja.smartwatchextensions.watchmanager.ui

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

/**
 * Adds all destinations used by [WatchManagerScreen] to the [NavGraphBuilder]. The entry point is
 * the route defined by [route].
 * @param modifier [Modifier].
 * @param route The entry point for this route.
 */
fun NavGraphBuilder.watchManagerGraph(
    onNavigateTo: (String) -> Unit,
    route: String,
    modifier: Modifier = Modifier,
) {
    composable(route) {
        WatchManagerScreen(
            onNavigateTo = {
                onNavigateTo(it.toString())
            },
            modifier = modifier
        )
    }
    composable(WatchManagerDestination.AddNewWatch.name) {
        // TODO
    }
}

enum class WatchManagerDestination {
    AddNewWatch
}
