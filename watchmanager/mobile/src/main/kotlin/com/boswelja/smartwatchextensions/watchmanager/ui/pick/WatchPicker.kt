package com.boswelja.smartwatchextensions.watchmanager.ui.pick

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.boswelja.watchconnection.common.Watch
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun WatchPicker(
    onRegisterNewWatch: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: WatchPickerViewModel = getViewModel()
) {
    val watches by viewModel.watches.collectAsState()
    val selectedWatch by viewModel.selectedWatch.collectAsState()

    AnimatedContent(
        targetState = watches,
        transitionSpec = { fadeIn() with fadeOut() }
    ) {
        if (it != null) {
            if (it.isEmpty()) {
                NoWatches(
                    onClick = onRegisterNewWatch,
                    modifier = modifier.padding(contentPadding)
                )
            } else {
                WatchList(
                    watches = it,
                    selectedWatch = selectedWatch,
                    onClick = viewModel::selectWatch,
                    onNewWatchClick = onRegisterNewWatch,
                    modifier = modifier,
                    contentPadding = contentPadding
                )
            }
        } else {
            // TODO Loading
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoWatches(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Add, null)
            Text("No watches configured! Tap to add")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchList(
    watches: List<Watch>,
    selectedWatch: Watch?,
    onClick: (Watch) -> Unit,
    onNewWatchClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues()
) {
    val itemModifier = Modifier
        .aspectRatio(0.5f)
        .width(120.dp)
    LazyRow(
        modifier = modifier,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(watches) {
            OutlinedCard(
                onClick = { onClick(it) },
                modifier = itemModifier
            ) {
                Column(Modifier.fillMaxSize()) {
                    Icon(
                        Icons.Default.Watch,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                    Text(
                        text = it.name,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
        item {
            OutlinedCard(
                onClick = onNewWatchClick,
                modifier = itemModifier
            ) {
                Column(Modifier.fillMaxSize()) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                    Text(
                        text = "Add a new watch",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}
