package com.boswelja.devicemanager.common.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.boswelja.devicemanager.R

@Composable
fun LoadingScreen(progress: Float) {
    Column(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (progress > 0.0f) {
            LinearProgressIndicator(progress = progress)
        } else {
            LinearProgressIndicator()
        }
        Text(stringResource(R.string.dnd_sync_helper_loading_text))
    }
}
