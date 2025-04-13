package com.boswelja.smartwatchextensions.watchmanager.ui.pick

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.CardDefaults.outlinedCardColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.boswelja.smartwatchextensions.core.watches.Watch
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun WatchPicker(
    onRegisterNewWatch: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: WatchPickerViewModel = koinViewModel()
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
        colors = outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(WatchPickerDefaults.ContentPadding),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Add, null)
            Text("No watches configured! Tap to add")
        }
    }
}

@Composable
fun WatchList(
    watches: List<Watch>,
    selectedWatch: Watch?,
    onClick: (Watch) -> Unit,
    onNewWatchClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues()
) {
    LazyRow(
        modifier = modifier.height(
            WatchPickerDefaults.FocusedCardHeight +
                    contentPadding.calculateBottomPadding() +
                    contentPadding.calculateTopPadding()
        ),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        items(
            items = watches,
            key = { it.uid }
        ) {
            val height by animateDpAsState(
                targetValue = if (it == selectedWatch) {
                    WatchPickerDefaults.FocusedCardHeight
                } else {
                    WatchPickerDefaults.CardHeight
                }
            )
            val color by animateColorAsState(
                targetValue = if (it == selectedWatch) {
                    MaterialTheme.colorScheme.primaryContainer
                } else  {
                    MaterialTheme.colorScheme.secondaryContainer
                }
            )
            WatchPickerCard(
                image = {
                    Icon(
                        Icons.Default.Watch,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                },
                text = {
                    Text(
                        text = it.name,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                onClick = { onClick(it) },
                containerColor = color,
                modifier = Modifier
                    .height(height)
                    .aspectRatio(WatchPickerDefaults.CardAspectRatio)
            )
        }
        item {
            WatchPickerCard(
                image = {
                    Icon(
                        Icons.Default.Add,
                        null,
                        modifier = Modifier.fillMaxSize()
                    )
                },
                text = {
                    Text(text = "Add new")
                },
                onClick = onNewWatchClick,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier
                    .height(WatchPickerDefaults.CardHeight)
                    .aspectRatio(WatchPickerDefaults.CardAspectRatio)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchPickerCard(
    image: @Composable () -> Unit,
    text: @Composable () -> Unit,
    onClick: () -> Unit,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onClick,
        colors = outlinedCardColors(containerColor = containerColor),
        modifier = modifier
    ) {
        Column(Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                image()
            }
            Box(Modifier.padding(WatchPickerDefaults.ContentPadding)) {
                ProvideTextStyle(MaterialTheme.typography.titleMedium) {
                    text()
                }
            }
        }
    }
}

object WatchPickerDefaults {
    val FocusedCardHeight = 172.dp

    val CardHeight = 156.dp

    val ContentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)

    const val CardAspectRatio = 0.7f
}
