package com.boswelja.smartwatchextensions.dashboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A Row with some common parameters for displaying a small feature summary.
 * @param modifier [Modifier].
 * @param icon The feature icon Composable.
 * @param text The feature text Composable.
 */
@Composable
fun FeatureSummarySmall(
    modifier: Modifier = Modifier,
    icon: @Composable RowScope.() -> Unit,
    text: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        icon()
        text()
    }
}
