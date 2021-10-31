package com.boswelja.smartwatchextensions.onboarding.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.boswelja.watchconnection.common.Watch
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterWatchScreen(
    availableWatches: List<Watch>,
    onWatchRegistered: () -> Unit,
    modifier: Modifier = Modifier
) {
    var registeringWatch by remember {
        mutableStateOf<Watch?>(null)
    }
    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = {
                    Text("Add a Watch")
                }
            )
        }
    ) {
        AvailableWatchList(
            watches = availableWatches,
            onWatchSelected = { watch ->
                registeringWatch = watch
            },
            modifier = modifier
        )
    }
    registeringWatch?.let {
        RegisteringWatch(
            watch = it,
            onComplete = { success ->
                if (success) onWatchRegistered()
                else registeringWatch = null
            }
        )
    }
}

@Composable
fun AvailableWatchList(
    watches: List<Watch>,
    onWatchSelected: (Watch) -> Unit,
    modifier: Modifier = Modifier,
    itemModifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement
    ) {
        items(watches) { watch ->
            AvailableWatch(
                watch = watch,
                onClick = { onWatchSelected(watch) },
                modifier = itemModifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun AvailableWatch(
    watch: Watch,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .heightIn(min = 56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Watch,
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
        )
        Spacer(Modifier.width(16.dp))
        Text(watch.name)
    }
}

@Composable
fun RegisteringWatch(
    watch: Watch,
    onComplete: (success: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel = getViewModel<OnboardingViewModel>()
    AlertDialog(
        modifier = modifier,
        onDismissRequest = { },
        confirmButton = { },
        title = {
            Text("Please Wait")
        },
        text = {
            Text("Registering your watch")
        }
    )
    LaunchedEffect(watch) {
        val success = viewModel.registerWatch(watch)
        onComplete(success)
    }
}
