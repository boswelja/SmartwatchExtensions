package com.boswelja.smartwatchextensions.aboutapp.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.activity
import androidx.navigation.compose.composable

/**
 * Adds destinations for "About App" to the calling graph.
 */
fun NavGraphBuilder.aboutAppGraph(
    graphRoute: String,
    onNavigateTo: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues()
) {
    activity(AboutAppDestinations.PRIVACY_POLICY.route) {
        action = Intent.ACTION_VIEW
        data = Uri.parse("https://github.com/boswelja/SmartwatchExtensions/blob/main/PRIVACY.md")
    }
    activity(AboutAppDestinations.SOURCE.route) {
        action = Intent.ACTION_VIEW
        data = Uri.parse("https://github.com/boswelja/SmartwatchExtensions")
    }
    activity(AboutAppDestinations.CHANGELOG.route) {
        action = Intent.ACTION_VIEW
        data = Uri.parse("https://github.com/boswelja/SmartwatchExtensions/releases")
    }
    composable(graphRoute) {
        AboutAppScreen(
            onNavigateTo = onNavigateTo,
            modifier = modifier,
            contentPadding = contentPadding
        )
    }
}

/**
 * Contains possible destinations for "About App"
 * @param route The route to the
 */
enum class AboutAppDestinations(val route: String) {
    PRIVACY_POLICY("privacy"),
    SOURCE("source"),
    CHANGELOG("changelog")
}
