package com.boswelja.smartwatchextensions.watchmanager.ui

import android.os.Bundle
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.boswelja.smartwatchextensions.watchmanager.ui.manageregistered.ManageRegisteredWatchScreen
import com.boswelja.smartwatchextensions.watchmanager.ui.manageregistered.ManageRegisteredWatchViewModel
import com.boswelja.smartwatchextensions.watchmanager.ui.register.RegisterWatchScreen
import org.koin.androidx.compose.getStateViewModel

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
    composable("${WatchManagerDestination.ManageRegisteredWatch.name}/{watchUid}",) {
        val viewModel = getStateViewModel<ManageRegisteredWatchViewModel>(state = { it.arguments ?: Bundle.EMPTY })
        ManageRegisteredWatchScreen(modifier, viewModel)
    }
}

enum class WatchManagerDestination {
    AddNewWatch,
    ManageRegisteredWatch
}
