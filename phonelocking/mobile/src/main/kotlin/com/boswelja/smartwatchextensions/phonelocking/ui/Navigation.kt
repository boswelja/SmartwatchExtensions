package com.boswelja.smartwatchextensions.phonelocking.ui

import android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.activity
import androidx.navigation.compose.composable
import androidx.navigation.navigation

/**
 * Adds the phone locking nav graph to the calling NavGraphBuilder.
 * @param graphRoute The route to be called when entering this nav graph.
 * @param onNavigate Called when navigation should occur.
 * @param screenModifier The universal [Modifier] to apply to all screens.
 */
fun NavGraphBuilder.phoneLockingNavigation(
    graphRoute: String,
    onNavigate: (route: String) -> Unit,
    screenModifier: Modifier = Modifier
) = navigation(
    PhoneLockingDestination.PHONE_LOCKING_SETTINGS.route,
    graphRoute
) {
    composable(PhoneLockingDestination.PHONE_LOCKING_SETTINGS.route) {
        PhoneLockingSettingsScreen(
            onNavigate = onNavigate,
            modifier = screenModifier
        )
    }
    activity(PhoneLockingDestination.ACCESSIBILITY_SETTINGS.route) {
        action = ACTION_ACCESSIBILITY_SETTINGS
    }
}

internal enum class PhoneLockingDestination(val route: String) {
    PHONE_LOCKING_SETTINGS("phone_locking_settings"),
    ACCESSIBILITY_SETTINGS("accessibility_settings")
}
