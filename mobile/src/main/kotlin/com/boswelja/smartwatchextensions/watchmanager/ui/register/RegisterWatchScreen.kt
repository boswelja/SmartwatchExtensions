package com.boswelja.smartwatchextensions.watchmanager.ui.register

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.smartwatchextensions.R
import com.boswelja.watchconnection.core.Watch

@Composable
fun RegisterWatchScreen(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp
) {
    val viewModel: RegisterWatchViewModel = viewModel()
    val discoveredWatches = viewModel.discoveredWatches

    Column(
        modifier.padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(contentPadding)
    ) {
        RegisterWatchesHeader(
            modifier = Modifier.padding(contentPadding)
        )
        Card {
            Column(
                verticalArrangement = Arrangement.spacedBy(contentPadding)
            ) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
                DiscoveredWatchesList(
                    discoveredWatches = discoveredWatches
                )
            }
        }
    }
}

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
