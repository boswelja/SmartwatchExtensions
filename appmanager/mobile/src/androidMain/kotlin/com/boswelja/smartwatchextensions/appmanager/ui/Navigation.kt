package com.boswelja.smartwatchextensions.appmanager.ui

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation

/**
 * Adds the App Manager navigation graph to the host Nav Graph.
 */
fun NavGraphBuilder.appManagerNavigation(
    graphRoute: String,
    onShowSnackbar: suspend (String) -> Unit,
    onNavigate: (route: String) -> Unit,
    screenModifier: Modifier = Modifier
) = navigation(
    AppManagerDestinations.APP_MANAGER_LIST.route,
    graphRoute
) {
    composable(AppManagerDestinations.APP_MANAGER_LIST.route) {
        AppListScreen(
            onAppClicked = {
                onNavigate(
                    AppManagerDestinations.APP_INFO.route
                        .replace("{package-name}", it.packageName)
                )
            },
            modifier = screenModifier
        )
    }
    composable(
        route = AppManagerDestinations.APP_INFO.route,
        arguments = listOf(
            navArgument("package-name") {
                nullable = false
                type = NavType.StringType
            }
        )
    ) {
        val packageName = it.arguments!!.getString("package-name")!!
        AppInfoScreen(
            packageName = packageName,
            onShowSnackbar = onShowSnackbar,
            modifier = screenModifier
        )
    }
}

internal enum class AppManagerDestinations(val route: String) {
    APP_MANAGER_LIST("app-list"),
    APP_INFO("app-info/{package-name}")
}
