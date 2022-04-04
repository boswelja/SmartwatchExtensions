package com.boswelja.smartwatchextensions.devicemanagement.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.ui.Card
import com.boswelja.smartwatchextensions.common.ui.CardHeader
import com.boswelja.smartwatchextensions.core.ui.list.ListItem
import com.boswelja.smartwatchextensions.devicemanagement.ui.info.WatchInfoScreen
import com.boswelja.watchconnection.common.Watch
import kotlinx.coroutines.Dispatchers
import org.koin.androidx.compose.getViewModel

/**
 * A Composable screen for displaying Watch Manager actions.
 * @param modifier [Modifier].
 * @param contentPadding The screen padding.
 * @param onShowSnackbar Called when a snackbar should be shown.
 * @param onNavigateTo Called when navigation is requested.
 */
@Composable
fun WatchManagerScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
    onShowSnackbar: suspend (String) -> Unit,
    onNavigateTo: (WatchManagerDestination) -> Unit
) {
    val viewModel: WatchManagerViewModel = getViewModel()
    val registeredWatches by viewModel.registeredWatches.collectAsState(emptyList(), Dispatchers.IO)

    var visibleWatch by rememberSaveable { mutableStateOf<Watch?>(null) }

    BackHandler(enabled = visibleWatch != null) {
        visibleWatch = null
    }

    Crossfade(targetState = visibleWatch) { watch ->
        if (watch != null) {
            WatchInfoScreen(
                modifier = modifier,
                contentPadding = contentPadding,
                watch = watch,
                onShowSnackbar = onShowSnackbar,
                onWatchRemoved = { visibleWatch = null }
            )
        } else {
            WatchManagerCard(
                modifier = modifier,
                contentPadding = contentPadding,
                registeredWatches = registeredWatches,
                onWatchSelected = { visibleWatch = it },
                onAddClicked = { onNavigateTo(WatchManagerDestination.REGISTER_WATCHES) }
            )
        }
    }
}

/**
 * A Composable for displaying Watch Manager settings.
 * @param modifier [Modifier].
 * @param contentPadding The content padding.
 * @param registeredWatches The list of registered watches.
 * @param onWatchSelected Called when a watch is selected.
 * @param onAddClicked Called when Add a Watch is clicked.
 */
@Composable
fun WatchManagerCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
    registeredWatches: List<Watch>,
    onWatchSelected: (Watch) -> Unit,
    onAddClicked: () -> Unit
) {
    Box(modifier.padding(contentPadding)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            header = {
                CardHeader(
                    title = { Text(stringResource(R.string.watch_manager_registered_watch_header)) }
                )
            }
        ) {
            LazyColumn {
                items(registeredWatches) { watch ->
                    ListItem(
                        text = { Text(watch.name) },
                        icon = {
                            Icon(
                                Icons.Outlined.Watch,
                                null,
                                Modifier.size(40.dp)
                            )
                        },
                        modifier = Modifier.clickable { onWatchSelected(watch) }
                    )
                }
                item {
                    ListItem(
                        text = { Text(stringResource(R.string.watch_manager_add_watch_title)) },
                        icon = { Icon(Icons.Outlined.Add, null) },
                        modifier = Modifier.clickable(onClick = onAddClicked)
                    )
                }
            }
        }
    }
}
