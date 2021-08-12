package com.boswelja.smartwatchextensions.watchmanager.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.ui.Card
import com.boswelja.smartwatchextensions.common.ui.CardHeader
import com.boswelja.smartwatchextensions.watchmanager.ui.info.WatchInfoScreen
import com.boswelja.watchconnection.core.Watch
import kotlinx.coroutines.Dispatchers

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun WatchManagerScreen(
    modifier: Modifier = Modifier,
    onShowSnackbar: suspend (String) -> Unit,
    onNavigateTo: (WatchManagerDestination) -> Unit
) {
    val viewModel: WatchManagerViewModel = viewModel()
    val registeredWatches by viewModel.registeredWatches.collectAsState(emptyList(), Dispatchers.IO)

    var visibleWatch by rememberSaveable { mutableStateOf<Watch?>(null) }

    BackHandler(enabled = visibleWatch != null) {
        visibleWatch = null
    }

    Card(
        modifier = modifier,
        header = {
            CardHeader(
                title = { Text(stringResource(R.string.watch_manager_registered_watch_header)) }
            )
        }
    ) {
        Column {
            registeredWatches.forEach { watch ->
                ListItem(
                    icon = {
                        Icon(
                            Icons.Outlined.Watch,
                            null,
                            Modifier.size(40.dp)
                        )
                    },
                    text = { Text(watch.name) },
                    modifier = Modifier.clickable {
                        visibleWatch = watch
                    }
                )
            }
            ListItem(
                text = {
                    Text(stringResource(R.string.watch_manager_add_watch_title))
                },
                icon = {
                    Icon(Icons.Outlined.Add, null)
                },
                modifier = Modifier.clickable {
                    onNavigateTo(WatchManagerDestination.REGISTER_WATCHES)
                }
            )
        }
    }

    visibleWatch?.let { watch ->
        WatchInfoScreen(
            modifier = Modifier.background(MaterialTheme.colors.background),
            watch = watch,
            onShowSnackbar = onShowSnackbar,
            onWatchRemoved = { visibleWatch = null }
        )
    }
}
