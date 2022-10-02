package com.boswelja.smartwatchextensions.watchmanager.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.koin.androidx.compose.getViewModel

@Composable
fun RegisterWatchScreen(
    onRegistrationFinished: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisterWatchViewModel = getViewModel()
) {
    // TODO
}
