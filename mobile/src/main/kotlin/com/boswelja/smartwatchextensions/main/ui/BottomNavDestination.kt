package com.boswelja.smartwatchextensions.main.ui

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.boswelja.smartwatchextensions.R

/**
 * All top-level destinations to be shown in the bottom navigation bar.
 * @param route The navigation route for the destination.
 * @param labelRes The string resource used to label the destination.
 * @param icon The destination icon.
 */
enum class BottomNavDestination(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector
) {
    DASHBOARD(
        "dashboard",
        R.string.bottom_nav_dashboard_label,
        Icons.Default.Dashboard
    ),
    SETTINGS(
        "settings",
        R.string.bottom_nav_settings_label,
        Icons.Default.Settings
    ),
    ABOUT(
        "about",
        R.string.bottom_nav_about_label,
        Icons.Default.Info
    )
}

/**
 * A Composable for displaying the bottom navigation bar.
 * @param currentDestination The current destination.
 * @param onNavigateTo Called when the user navigates to another destination.
 */
@Composable
fun BottomNavBar(
    currentDestination: NavDestination?,
    onNavigateTo: (BottomNavDestination) -> Unit
) {
    NavigationBar {
        BottomNavDestination.values().forEach { destination ->
            val selected = currentDestination
                ?.hierarchy
                ?.any { it.route == destination.route } ?: false
            NavigationBarItem(
                selected = selected,
                icon = { Icon(destination.icon, null) },
                label = { Text(stringResource(destination.labelRes)) },
                onClick = { onNavigateTo(destination) }
            )
        }
    }
}
