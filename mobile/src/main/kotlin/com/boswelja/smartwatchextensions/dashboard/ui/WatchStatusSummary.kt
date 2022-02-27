package com.boswelja.smartwatchextensions.dashboard.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.core.ui.dialog.ConfirmationDialog
import com.boswelja.watchconnection.common.Watch
import com.boswelja.watchconnection.common.discovery.ConnectionMode

/**
 * A Composable for displaying the selected watch and it's status status.
 * @param modifier [Modifier].
 * @param watchStatus The watch status to display info for.
 */
@Composable
fun SelectedWatchSummary(
    watchStatus: ConnectionMode,
    registeredWatches: List<Watch>,
    selectedWatch: Watch?,
    onWatchSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var dialogVisible by remember { mutableStateOf(false) }
    val (icon, label) = if (watchStatus == ConnectionMode.Disconnected) {
        Pair(Icons.Outlined.Sync, stringResource(R.string.watch_status_connecting))
    } else {
        Pair(
            Icons.Outlined.CheckCircleOutline,
            stringResource(R.string.watch_status_connected)
        )
    }
    Column(
        Modifier
            .clickable { dialogVisible = true }
            .then(modifier)
    ) {
        Text(selectedWatch?.name ?: "")
        Row {
            Icon(icon, null)
            Text(label)
        }
    }

    if (dialogVisible) {
        ConfirmationDialog(
            title = { Text("Select Watch") },
            onDismissRequest = { dialogVisible = false },
            itemContent = {
                Text(it.name)
            },
            items = registeredWatches,
            selectedItem = selectedWatch,
            onItemSelectionChanged = { onWatchSelected(it.uid) }
        )
    }
}
