package com.boswelja.smartwatchextensions.managespace.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.AlertDialog
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.ui.AppTheme
import com.boswelja.smartwatchextensions.common.ui.UpNavigationAppBar

/**
 * An activity to present the user with options for resetting various data used by Wearable
 * Extensions.
 */
class ManageSpaceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                var progress by remember { mutableStateOf(0f) }
                val scaffoldState = rememberScaffoldState()
                Scaffold(
                    scaffoldState = scaffoldState,
                    topBar = {
                        UpNavigationAppBar(
                            title = { Text(stringResource(R.string.manage_space_title)) },
                            onNavigateUp = { finish() }
                        )
                    }
                ) {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .scrollable(rememberScrollState(), Orientation.Vertical)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ClearCacheAction(
                            scaffoldState = scaffoldState,
                            onProgressChange = { progress = it }
                        )
                        ResetAnalyticsAction(scaffoldState = scaffoldState)
                        ResetAppSettingsAction(
                            scaffoldState = scaffoldState,
                            onProgressChange = { progress = it }
                        )
                        ResetExtensionsAction(
                            scaffoldState = scaffoldState,
                            onProgressChange = { progress = it }
                        )
                        ResetAppAction(
                            scaffoldState = scaffoldState,
                            onProgressChange = { progress = it }
                        )
                    }
                    if (progress > 0.0f) {
                        AlertDialog(
                            onDismissRequest = { if (progress >= 1f) progress = 0f },
                            title = { Text(stringResource(R.string.please_wait)) },
                            text = { LinearProgressIndicator(progress, Modifier.fillMaxWidth()) },
                            buttons = { }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RequestDialog(
    title: String,
    text: String,
    onRequestGranted: () -> Unit,
    onRequestDenied: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onRequestDenied,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            TextButton(onClick = onRequestGranted) {
                Text(stringResource(R.string.dialog_button_reset))
            }
        },
        dismissButton = {
            TextButton(onClick = onRequestDenied) {
                Text(stringResource(R.string.dialog_button_cancel))
            }
        }
    )
}
