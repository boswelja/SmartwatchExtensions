package com.boswelja.smartwatchextensions.dashboard.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.core.ui.dialog.ConfirmationDialog
import com.boswelja.watchconnection.common.Watch
import com.boswelja.watchconnection.common.discovery.ConnectionMode

/**
 * A Composable for displaying the selected watch and it's status status.
 * @param contentModifier [Modifier].
 * @param watchStatus The watch status to display info for.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectedWatchSummary(
    watchStatus: ConnectionMode,
    registeredWatches: List<Watch>,
    selectedWatch: Watch?,
    onWatchSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier
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
    Card(
        onClick = { dialogVisible = true },
        modifier = modifier
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .then(contentModifier)) {
            Text(
                text = selectedWatch?.name ?: "",
                style = MaterialTheme.typography.labelMedium
            )

            Spacer(Modifier.height(2.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, null)
                Spacer(modifier = Modifier.width(4.dp))
                Text(label)
            }
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
