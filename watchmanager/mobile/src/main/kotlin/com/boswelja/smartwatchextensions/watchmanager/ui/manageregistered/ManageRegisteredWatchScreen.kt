package com.boswelja.smartwatchextensions.watchmanager.ui.manageregistered

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.boswelja.watchconnection.common.Watch
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ManageRegisteredWatchScreen(
    modifier: Modifier = Modifier,
    viewModel: ManageRegisteredWatchViewModel = getViewModel()
) {
    val watch by viewModel.watch.collectAsState()

    AnimatedContent(
        targetState = watch,
        transitionSpec = { fadeIn() with fadeOut() }
    ) {
        if (it == null) {
            LoadingScreen(modifier = modifier)
        } else {
            ManageRegisteredWatch(
                watch = it,
                onUpdateWatchName = viewModel::renameWatch,
                modifier = modifier
            )
        }
    }
}

@Composable
internal fun LoadingScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ManageRegisteredWatch(
    watch: Watch,
    onUpdateWatchName: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var updatedName by remember(watch) { mutableStateOf(watch.name) }
    val isUpdatedNameValid by remember(updatedName) {
        derivedStateOf { updatedName.isNotBlank() }
    }
    val canSaveName by remember(watch, updatedName, isUpdatedNameValid) {
        derivedStateOf { isUpdatedNameValid && watch.name != updatedName }
    }
    Column(modifier) {
        Row {
            OutlinedTextField(
                value = updatedName,
                onValueChange = { updatedName = it },
                isError = !isUpdatedNameValid
            )
            TextButton(
                onClick = { onUpdateWatchName(updatedName) },
                enabled = canSaveName
            ) {
                Text("Save")
            }
        }
    }
}
