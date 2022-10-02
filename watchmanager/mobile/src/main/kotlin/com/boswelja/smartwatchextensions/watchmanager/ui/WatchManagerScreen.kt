package com.boswelja.smartwatchextensions.watchmanager.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import com.boswelja.smartwatchextensions.core.ui.LoadingIndicator
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun WatchManagerScreen(
    onNavigateTo: (WatchManagerDestination) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WatchManagerViewModel = getViewModel()
) {
    val registeredWatches by viewModel.registeredWatches.collectAsState(initial = null)
    Column {
        LoadingIndicator(
            isLoading = registeredWatches == null,
            modifier = Modifier.fillMaxWidth()
        )
        registeredWatches?.let {
            LazyColumn(modifier = modifier) {
                items(
                    items = it,
                    key = { it.uid }
                ) { watch ->
                    val appVersion by produceState<String?>(initialValue = null, viewModel, watch) {
                        val versionResult = viewModel.loadWatchVersion(watch.uid)
                        value = versionResult.getOrDefault("Failed to load version")
                    }
                    ListItem(
                        headlineText = {
                            Text(text = watch.name)
                        },
                        supportingText = {
                            AnimatedContent(targetState = appVersion) { version ->
                                if (version != null) {
                                    Text(text = version)
                                } else {
                                    LinearProgressIndicator()
                                }
                            }
                        },
                        leadingContent = {
                            Icon(Icons.Default.Watch, contentDescription = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {  }
                    )
                }
                item {
                    ListItem(
                        headlineText = {
                            Text("Add a Watch")
                        },
                        leadingContent = {
                            Icon(Icons.Default.Add, contentDescription = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateTo(WatchManagerDestination.AddNewWatch) }
                    )
                }
            }
        }
    }
}
