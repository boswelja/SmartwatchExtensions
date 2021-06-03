package com.boswelja.smartwatchextensions.appmanager.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.boswelja.smartwatchextensions.R
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@Composable
fun Error(viewModel: AppManagerViewModel) {
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Outlined.ErrorOutline,
            null,
            modifier = Modifier.size(180.dp)
        )
        Text(
            stringResource(R.string.app_manager_error_title),
            style = MaterialTheme.typography.h4
        )
        Text(
            stringResource(R.string.app_manager_error_desc),
            style = MaterialTheme.typography.h6
        )
        OutlinedButton(onClick = { viewModel.startAppManagerService() }) {
            Icon(Icons.Outlined.Refresh, null)
            Text(stringResource(R.string.button_retry))
        }
    }
}
