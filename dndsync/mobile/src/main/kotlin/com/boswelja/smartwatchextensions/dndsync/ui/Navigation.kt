package com.boswelja.smartwatchextensions.dndsync.ui

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation

/**
 * Adds the DnD Sync navigation graph to the host Nav Graph.
 */
fun NavGraphBuilder.dndSyncNavigation(
    graphRoute: String,
    screenModifier: Modifier = Modifier
) = navigation(
    DnDSyncDestinations.DND_SYNC_SETTINGS.route,
    graphRoute
) {
    composable(DnDSyncDestinations.DND_SYNC_SETTINGS.route) {
        DnDSyncSettingsScreen(
            modifier = screenModifier
        )
    }
}

internal enum class DnDSyncDestinations(val route: String) {
    DND_SYNC_SETTINGS("dnd_sync_settings")
}
