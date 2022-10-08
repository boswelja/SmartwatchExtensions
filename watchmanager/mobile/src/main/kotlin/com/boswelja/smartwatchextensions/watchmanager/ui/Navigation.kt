package com.boswelja.smartwatchextensions.watchmanager.ui

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.boswelja.smartwatchextensions.watchmanager.ui.manageregistered.ManageRegisteredWatchScreen
import com.boswelja.smartwatchextensions.watchmanager.ui.register.RegisterWatchScreen

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
            onWatchClick = {
                onNavigateTo("${WatchManagerDestination.ManageRegisteredWatch.name}/${it.uid}")
            },
            onAddWatchClick = {
                onNavigateTo(WatchManagerDestination.AddNewWatch.name)
            },
            modifier = modifier
        )
    }
    composable(WatchManagerDestination.AddNewWatch.name) {
        RegisterWatchScreen(
            onRegistrationFinished = {
                // TODO
            },
            modifier = modifier
        )
    }
    composable(
        route = "${WatchManagerDestination.ManageRegisteredWatch.name}/{watch-uid}",
        arguments = listOf(
            navArgument("watch-uid") {
                type = NavType.StringType
                nullable = false
            }
        )
    ) {
        val watchUid = it.arguments!!.getString("watch-uid")!!
        ManageRegisteredWatchScreen(
            watchUid = watchUid,
            modifier = modifier
        )
    }
}

enum class WatchManagerDestination {
    AddNewWatch,
    ManageRegisteredWatch
}
