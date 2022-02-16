package com.boswelja.smartwatchextensions.messages.ui

import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarResult
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable

/**
 * All Messages navigation destinations.
 * @param route The navigation route.
 */
enum class MessageDestination(
    val route: String
) {
    MessageHistory("message-history")
}

/**
 * Adds all destinations used by [MessagesScreen] to the [NavGraphBuilder]. The entry point is
 * the route defined by [route].
 * @param modifier [Modifier].
 * @param contentPadding The padding for content.
 * @param navController [NavHostController].
 * @param route The entry point for this route.
 */
fun NavGraphBuilder.messagesGraph(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp,
    navController: NavHostController,
    route: String,
    onShowSnackbar: suspend (String, String?, SnackbarDuration) -> SnackbarResult
) {
    composable(route) {
        MessagesScreen(
            modifier = modifier,
            contentPadding = contentPadding,
            onShowSnackbar = { text, duration -> onShowSnackbar(text, null, duration) },
            onNavigateTo = { navController.navigate(it.route) }
        )
    }
    composable(MessageDestination.MessageHistory.route) {
        MessageHistoryScreen(
            modifier = modifier,
            contentPadding = contentPadding,
            onShowSnackbar = { onShowSnackbar(it, null, SnackbarDuration.Short) }
        )
    }
}
