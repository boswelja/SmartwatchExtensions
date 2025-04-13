package com.boswelja.smartwatchextensions.dashboard.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.core.ui.dialog.ConfirmationDialog
import com.boswelja.smartwatchextensions.core.ui.snackbarVisuals
import com.boswelja.smartwatchextensions.core.watches.Watch
import com.boswelja.smartwatchextensions.core.watches.status.ConnectionMode

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
    onShowSnackbar: suspend (SnackbarVisuals) -> Unit,
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var dialogVisible by remember { mutableStateOf(false) }

    LaunchedEffect(watchStatus) {
        val snackbar = when (watchStatus) {
            ConnectionMode.Disconnected ->
                snackbarVisuals(
                    message = context.getString(R.string.watch_status_connecting, selectedWatch?.name),
                    duration = SnackbarDuration.Indefinite,
                    withDismissAction = true
                )
            ConnectionMode.Internet,
            ConnectionMode.Bluetooth ->
                snackbarVisuals(
                    message = context.getString(R.string.watch_status_connected, selectedWatch?.name),
                    duration = SnackbarDuration.Short,
                    withDismissAction = true
                )
        }
        onShowSnackbar(snackbar)
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
