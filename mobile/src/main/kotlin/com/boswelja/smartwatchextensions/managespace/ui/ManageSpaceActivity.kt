package com.boswelja.smartwatchextensions.managespace.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.ui.AppTheme
import com.boswelja.smartwatchextensions.common.ui.UpNavigationAppBar
import kotlinx.coroutines.launch

// TODO A refactor is in order

/**
 * An activity to present the user with options for resetting various data used by Wearable
 * Extensions.
 */
class ManageSpaceActivity : ComponentActivity() {

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

/**
 * A Composable screen for displaying Manage Space options.
 * @param modifier [Modifier].
 * @param actionModifier A [Modifier] to apply to actions.
 * @param onShowSnackbar Called when a snackbar should be shown.
 * @param onAppReset Called when the app should be reset.
 * @param contentPadding The screen content padding.
 */
@Composable
fun ManageSpaceScreen(
    modifier: Modifier = Modifier,
    actionModifier: Modifier = Modifier,
    onShowSnackbar: (String) -> Unit,
    onAppReset: () -> Unit,
    contentPadding: Dp = 16.dp
) {
    val context = LocalContext.current

    Column(
        modifier
            .scrollable(rememberScrollState(), Orientation.Vertical)
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ClearCacheAction(
            modifier = actionModifier,
            onActionFinished = { success ->
                val message = if (success) context.getString(R.string.clear_cache_success)
                else context.getString(R.string.clear_cache_failed)
                onShowSnackbar(message)
            },
            onProgressChange = { }
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
                val message = if (success) context.getString(R.string.reset_settings_success)
                else context.getString(R.string.reset_settings_failed)
                onShowSnackbar(message)
            },
            onProgressChange = { }
        )
        ResetExtensionsAction(
            modifier = actionModifier,
            onActionFinished = { success ->
                val message = if (success) context.getString(R.string.reset_extensions_success)
                else context.getString(R.string.reset_extensions_failed)
                onShowSnackbar(message)
            },
            onProgressChange = { }
        )
        ResetAppAction(
            modifier = actionModifier,
            onActionFinished = { success ->
                if (success) onAppReset()
                else onShowSnackbar(context.getString(R.string.reset_app_failed))
            },
            onProgressChange = { }
        )
    }
}
