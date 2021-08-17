package com.boswelja.smartwatchextensions.managespace.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.ui.AppTheme
import com.boswelja.smartwatchextensions.common.ui.ProgressDialog
import com.boswelja.smartwatchextensions.common.ui.UpNavigationAppBar
import kotlinx.coroutines.launch

/**
 * An activity to present the user with options for resetting various data used by Wearable
 * Extensions.
 */
class ManageSpaceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                val scaffoldState = rememberScaffoldState()
                val scope = rememberCoroutineScope()

                Scaffold(
                    scaffoldState = scaffoldState,
                    topBar = {
                        UpNavigationAppBar(
                            title = { Text(stringResource(R.string.manage_space_title)) },
                            onNavigateUp = { finish() }
                        )
                    }
                ) {
                    ManageSpaceScreen(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = 16.dp,
                        onAppReset = { finish() },
                        onShowSnackbar = { message ->
                            scope.launch {
                                scaffoldState.snackbarHostState.showSnackbar(message)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ManageSpaceScreen(
    modifier: Modifier = Modifier,
    actionModifier: Modifier = Modifier,
    onShowSnackbar: (String) -> Unit,
    onAppReset: () -> Unit,
    contentPadding: Dp = 16.dp
) {
    val context = LocalContext.current

    var progress by remember { mutableStateOf(0f) }

    Column(
        modifier
            .scrollable(rememberScrollState(), Orientation.Vertical)
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ClearCacheAction(
            modifier = actionModifier,
            onActionFinished = { success ->
                progress = 0f
                val message = if (success) context.getString(R.string.clear_cache_success)
                else context.getString(R.string.clear_cache_failed)
                onShowSnackbar(message)
            },
            onProgressChange = { progress = it }
        )
        ResetAnalyticsAction(
            modifier = actionModifier,
            onActionFinished = { success ->
                val message = if (success) context.getString(R.string.reset_analytics_success)
                else context.getString(R.string.reset_analytics_failed)
                onShowSnackbar(message)
            }
        )
        ResetAppSettingsAction(
            modifier = actionModifier,
            onActionFinished = { success ->
                progress = 0f
                val message = if (success) context.getString(R.string.reset_settings_success)
                else context.getString(R.string.reset_settings_failed)
                onShowSnackbar(message)
            },
            onProgressChange = { progress = it }
        )
        ResetExtensionsAction(
            modifier = actionModifier,
            onActionFinished = { success ->
                progress = 0f
                val message = if (success) context.getString(R.string.reset_extensions_success)
                else context.getString(R.string.reset_extensions_failed)
                onShowSnackbar(message)
            },
            onProgressChange = { progress = it }
        )
        ResetAppAction(
            modifier = actionModifier,
            onActionFinished = { success ->
                progress = 0f
                if (success) {
                    onAppReset()
                } else {
                    onShowSnackbar(context.getString(R.string.reset_app_failed))
                }
            },
            onProgressChange = { progress = it }
        )
    }
    if (progress > 0.0f) {
        ProgressDialog(
            onDismissRequest = { if (progress >= 1f) progress = 0f },
            title = { Text(stringResource(R.string.please_wait)) },
            progress = progress
        )
    }
}
