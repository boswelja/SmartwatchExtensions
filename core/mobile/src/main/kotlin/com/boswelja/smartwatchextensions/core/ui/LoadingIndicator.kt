package com.boswelja.smartwatchextensions.core.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A Composable for displaying a horizontal loading indicator.
 * @param modifier [Modifier].
 * @param isLoading Whether the loading indicator is visible.
 */
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
