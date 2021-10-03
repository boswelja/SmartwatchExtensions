package com.boswelja.smartwatchextensions.watchmanager.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.ui.FeatureSummarySmall
import com.boswelja.watchconnection.common.discovery.Status

@Composable
fun WatchStatusSummarySmall(
    modifier: Modifier = Modifier,
    watchStatus: Status
) {
    val (icon, label) = when (watchStatus) {
        Status.CONNECTING ->
            Pair(Icons.Outlined.Sync, stringResource(R.string.watch_status_connecting))
        Status.CONNECTED,
        Status.CONNECTED_NEARBY ->
            Pair(
                Icons.Outlined.CheckCircleOutline,
                stringResource(R.string.watch_status_connected)
            )
        Status.DISCONNECTED ->
            Pair(
                Icons.Outlined.ErrorOutline,
                stringResource(R.string.watch_status_disconnected)
            )
        Status.MISSING_APP ->
            Pair(
                Icons.Outlined.ErrorOutline,
                stringResource(R.string.watch_status_missing_app)
            )
        Status.ERROR ->
            Pair(
                Icons.Outlined.ErrorOutline,
                stringResource(R.string.watch_status_error)
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
