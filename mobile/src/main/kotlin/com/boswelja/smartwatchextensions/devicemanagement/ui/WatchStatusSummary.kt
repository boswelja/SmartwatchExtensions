package com.boswelja.smartwatchextensions.devicemanagement.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.ui.FeatureSummarySmall
import com.boswelja.watchconnection.common.discovery.ConnectionMode

/**
 * A Composable for displaying a small watch status summary.
 * @param modifier [Modifier].
 * @param watchStatus The watch status to display info for.
 */
@Composable
fun WatchStatusSummarySmall(
    modifier: Modifier = Modifier,
    watchStatus: ConnectionMode
) {
    val (icon, label) = if (watchStatus == ConnectionMode.Disconnected) {
        Pair(Icons.Outlined.Sync, stringResource(R.string.watch_status_connecting))
    } else {
        Pair(
            Icons.Outlined.CheckCircleOutline,
            stringResource(R.string.watch_status_connected)
        )
    }

    FeatureSummarySmall(
        modifier = modifier,
        icon = {
            Icon(icon, null)
        },
        text = {
            Text(label, modifier = Modifier.padding(start = 8.dp))
        }
    )
}
