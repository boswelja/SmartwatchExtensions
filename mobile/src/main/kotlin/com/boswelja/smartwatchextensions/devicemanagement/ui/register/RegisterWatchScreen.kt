package com.boswelja.smartwatchextensions.devicemanagement.ui.register

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.boswelja.smartwatchextensions.R
import com.boswelja.watchconnection.common.Watch
import org.koin.androidx.compose.getViewModel

/**
 * A Composable screen for displaying the watch registration flow.
 * @param modifier [Modifier].
 * @param onWatchRegistered Called when a watch was registered.
 */
@Composable
fun RegisterWatchScreen(
    modifier: Modifier = Modifier,
    onWatchRegistered: (() -> Unit)? = null
) {
    val viewModel: RegisterWatchViewModel = getViewModel()
    val discoveredWatches = viewModel.discoveredWatches

    Column(modifier) {
        RegisterWatchesHeader()
        Card {
            Column {
                LinearProgressIndicator(Modifier.fillMaxWidth())
                DiscoveredWatchesList(
                    discoveredWatches = discoveredWatches
                )
            }
        }
    }

    if (onWatchRegistered != null && discoveredWatches.isNotEmpty()) {
        LaunchedEffect(key1 = discoveredWatches) {
            onWatchRegistered()
        }
    }
}

/**
 * A Composable to display a watch registration flow header.
 * @param modifier [Modifier].
 */
@Composable
fun RegisterWatchesHeader(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(R.string.register_watch_title),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.h5,
        )
        Text(
            stringResource(R.string.register_watch_desc),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.body1,
        )
    }
}

/**
 * A Composable for displaying a list of discovered watches.
 * @param modifier [Modifier].
 * @param contentPadding The content padding.
 * @param discoveredWatches The list of discovered watches to display.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DiscoveredWatchesList(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp,
    discoveredWatches: List<Watch>?
) {
    if (discoveredWatches.isNullOrEmpty()) {
        Text(
            stringResource(R.string.register_watch_no_watches),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.body2,
            modifier = Modifier.padding(contentPadding)
        )
    } else {
        LazyColumn(
            modifier = modifier
        ) {
            items(discoveredWatches) { watch ->
                ListItem(
                    text = { Text(watch.name) },
                    icon = {
                        Icon(
                            Icons.Outlined.Watch,
                            null,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                )
            }
        }
    }
}
