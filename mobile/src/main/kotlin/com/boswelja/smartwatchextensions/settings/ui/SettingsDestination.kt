package com.boswelja.smartwatchextensions.settings.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.activity
import androidx.navigation.compose.composable
import com.boswelja.smartwatchextensions.watchmanager.ui.watchManagerGraph

/**
 * All destinations reachable from withing App Settings.
 * @param route The navigation route for the destination.
 */
enum class SettingsDestination(
    val route: String
) {
    WATCH_MANAGER("watch-manager"),
    PRIVACY_POLICY("privacy"),
    SOURCE("source"),
    CHANGELOG("changelog")
}

/**
 * Adds all destinations used by [AppSettingsScreen] to the [NavGraphBuilder]. The entry point is
 * the route defined by [route].
 * @param modifier [Modifier].
 * @param route The entry point for this route.
 */
fun NavGraphBuilder.appSettingsGraph(
    modifier: Modifier = Modifier,
    onNavigateTo: (String) -> Unit,
    route: String
) {
    activity(SettingsDestination.PRIVACY_POLICY.route) {
        action = Intent.ACTION_VIEW
        data = Uri.parse("https://github.com/boswelja/SmartwatchExtensions/blob/main/PRIVACY.md")
    }
    activity(SettingsDestination.SOURCE.route) {
        action = Intent.ACTION_VIEW
        data = Uri.parse("https://github.com/boswelja/SmartwatchExtensions")
    }
    activity(SettingsDestination.CHANGELOG.route) {
        action = Intent.ACTION_VIEW
        data = Uri.parse("https://github.com/boswelja/SmartwatchExtensions/releases")
    }
    composable(route) {
        AppSettingsScreen(
            modifier = modifier,
            onNavigateTo = { onNavigateTo(it.route) }
        )
    }
    watchManagerGraph(
        modifier = modifier,
        onNavigateTo = onNavigateTo,
        route = SettingsDestination.WATCH_MANAGER.route
    )
}
