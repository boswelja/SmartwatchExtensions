package com.boswelja.smartwatchextensions.watchmanager.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import com.boswelja.smartwatchextensions.core.ui.LoadingIndicator
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchManagerScreen(
    modifier: Modifier = Modifier,
    viewModel: WatchManagerViewModel = getViewModel()
) {
    val registeredWatches by viewModel.registeredWatches.collectAsState(initial = null)
    Column(modifier = modifier) {
        LoadingIndicator(
            isLoading = registeredWatches == null,
            modifier = Modifier.fillMaxWidth()
        )
        registeredWatches?.let {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
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
                            appVersion?.let {
                                Text(text = it)
                            } ?: LoadingIndicator(isLoading = true)
                        }
                    )
                }
            }
        }
    }
}
