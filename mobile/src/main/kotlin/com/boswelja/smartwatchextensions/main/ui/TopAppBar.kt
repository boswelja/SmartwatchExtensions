package com.boswelja.smartwatchextensions.main.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraphBuilder
import androidx.navigation.activity
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.aboutapp.ui.aboutAppGraph
import com.boswelja.smartwatchextensions.settings.ui.appSettingsGraph

/**
 * An app bar with a navigate up action.
 * @param onNavigateUp Called when up navigation is requested.
 */
@Composable
fun TopAppBar(
    title: @Composable () -> Unit,
    canNavigateUp: Boolean,
    onNavigateUp: () -> Unit,
    onNavigateTo: (String) -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    MediumTopAppBar(
        title = title,
        navigationIcon = {
            AnimatedVisibility(
                visible = canNavigateUp,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                IconButton(onNavigateUp) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = null
                    )
                }
            }
        },
        actions = {
            AnimatedVisibility(
                visible = !canNavigateUp,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row {
                    IconButton(onClick = { onNavigateTo("settings") }) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = stringResource(R.string.app_bar_settings_label)
                        )
                    }
                    ToolbarOverflowAction(onNavigateTo = onNavigateTo)
                }
            }
        },
        scrollBehavior = scrollBehavior
    )
}

/**
 * An Overflow action for the top app bar. This displays an [IconButton] that opens a [DropdownMenu].
 */
@Composable
fun ToolbarOverflowAction(
    onNavigateTo: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var dropdownVisible by remember { mutableStateOf(false) }
    Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
        IconButton(
            onClick = { dropdownVisible = !dropdownVisible }
        ) {
            Icon(
                modifier = modifier,
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(R.string.app_bar_overflow_label)
            )
        }
        DropdownMenu(
            expanded = dropdownVisible,
            onDismissRequest = { dropdownVisible = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.app_bar_wiki_label)) },
                onClick = {
                    onNavigateTo(TopAppBarDestinations.WIKI.route)
                    dropdownVisible = false
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.app_bar_about_label)) },
                onClick = {
                    onNavigateTo(TopAppBarDestinations.ABOUT.route)
                    dropdownVisible = false
                }
            )
        }
    }
}

/**
 * Contains all possible destinations the top app bar can navigate to.
 * @param route The Composable route.
 */
enum class TopAppBarDestinations(val route: String) {
    SETTINGS("settings"),
    WIKI("wiki"),
    ABOUT("about")
}

/**
 * Adds the App Bar navigation graph to the calling [NavGraphBuilder].
 */
fun NavGraphBuilder.appBarGraph(
    onShowSnackbar: suspend (SnackbarVisuals) -> Unit,
    onNavigateTo: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues()
) {
    activity(TopAppBarDestinations.WIKI.route) {
        action = Intent.ACTION_VIEW
        data = Uri.parse("https://github.com/boswelja/SmartwatchExtensions/wiki")
    }
    aboutAppGraph(
        graphRoute = TopAppBarDestinations.ABOUT.route,
        onNavigateTo = onNavigateTo,
        modifier = modifier,
        contentPadding = contentPadding
    )

    // Load settings
    appSettingsGraph(
        modifier = modifier,
        contentPadding = contentPadding,
        onNavigateTo = onNavigateTo,
        route = TopAppBarDestinations.SETTINGS.route,
        onShowSnackbar = onShowSnackbar
    )
}
