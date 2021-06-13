package com.boswelja.smartwatchextensions.appmanager.ui

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.boswelja.smartwatchextensions.common.ui.FeatureSummarySmall

@Composable
fun AppSummarySmall(
    modifier: Modifier = Modifier,
    appCount: Int
) {
    FeatureSummarySmall(
        modifier = modifier,
        icon = {
            Icon(
                Icons.Outlined.Apps,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth(0.33f)
                    .aspectRatio(1f)
            )
        },
        text = {
            Text(
                appCount.toString(),
                style = MaterialTheme.typography.h5
            )
        }
    )
}
