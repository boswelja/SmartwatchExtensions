package com.boswelja.smartwatchextensions.main.ui

import androidx.annotation.StringRes
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.boswelja.smartwatchextensions.R

enum class BottomNavDestination(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector
) {
    DASHBOARD(
        "dashboard",
        R.string.bottom_nav_dashboard_label,
        Icons.Outlined.Dashboard
    ),
    MESSAGES(
        "messages",
        R.string.nav_messages_label,
        Icons.Outlined.Message
    ),
    SETTINGS(
        "settings",
        R.string.bottom_nav_settings_label,
        Icons.Outlined.Settings
    ),
    ABOUT(
        "about",
        R.string.bottom_nav_about_label,
        Icons.Outlined.Info
    )
}

@Composable
fun BottonNav(
    currentDestination: NavDestination?,
    onNavigateTo: (BottomNavDestination) -> Unit
) {
    BottomNavigation(
        backgroundColor = MaterialTheme.colors.background
    ) {
        BottomNavDestination.values().forEach { destination ->
            val selected = currentDestination
                ?.hierarchy
                ?.any { it.route == destination.route } ?: false
            BottomNavigationItem(
                selected = selected,
                icon = { Icon(destination.icon, null) },
                label = { Text(stringResource(destination.labelRes)) },
                onClick = { onNavigateTo(destination) },
                alwaysShowLabel = false,
                selectedContentColor = MaterialTheme.colors.primary,
                unselectedContentColor = MaterialTheme.colors.onSurface
                    .copy(alpha = ContentAlpha.medium)
            )
        }
    }
}
