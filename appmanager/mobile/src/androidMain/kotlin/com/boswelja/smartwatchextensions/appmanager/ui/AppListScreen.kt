package com.boswelja.smartwatchextensions.appmanager.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.boswelja.smartwatchextensions.appmanager.WatchAppWithIcon
import org.koin.androidx.compose.getViewModel

/**
 * A Composable screen to display App Manager.
 * @param onAppClicked Called when an app is clicked.
 * @param modifier [Modifier].
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppListScreen(
    onAppClicked: (WatchAppWithIcon) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AppManagerViewModel = getViewModel()
) {
    val userApps by viewModel.userApps.collectAsState()
    val disabledApps by viewModel.disabledApps.collectAsState()
    val systemApps by viewModel.systemApps.collectAsState()
    val isUpdatingCache by viewModel.isUpdatingCache.collectAsState()
    // Only check system apps (for now). It's effectively guaranteed we'll have some on any device
    val isLoading = systemApps.isEmpty() || isUpdatingCache

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LoadingIndicator(
            modifier = Modifier.fillMaxWidth(),
            isLoading = isLoading
        )
        AppList(
            userApps = userApps,
            disabledApps = disabledApps,
            systemApps = systemApps,
            onAppClick = onAppClicked
        )
    }
}

/**
 * A Composable for displaying a horizontal loading indicator.
 * @param modifier [Modifier].
 * @param isLoading Whether the loading indicator is visible.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    isLoading: Boolean
) {
    AnimatedVisibility(
        visible = isLoading,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        LinearProgressIndicator(modifier)
    }
}
