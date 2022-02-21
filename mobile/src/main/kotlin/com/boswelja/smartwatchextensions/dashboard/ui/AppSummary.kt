package com.boswelja.smartwatchextensions.dashboard.ui

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import com.boswelja.smartwatchextensions.R

private const val ICON_WIDTH_PERCENT = 0.33f

/**
 * A Composable for displaying a summary of apps installed on a watch.
 */
@Composable
fun AppSummarySmall(
    modifier: Modifier = Modifier,
    appCount: Long
) {
    val context = LocalContext.current
    FeatureSummarySmall(
        modifier = modifier,
        icon = {
            Icon(
                Icons.Outlined.Apps,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth(ICON_WIDTH_PERCENT)
                    .aspectRatio(1f)
            )
        },
        text = {
            Text(
                context.resources.getQuantityString(
                    R.plurals.app_manager_app_count, appCount.toInt(), appCount
                ),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge
            )
        }
    )
}
