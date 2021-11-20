package com.boswelja.smartwatchextensions.appmanager.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.boswelja.smartwatchextensions.appmanager.R
import com.boswelja.smartwatchextensions.appmanager.WatchAppDetails
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

/**
 * Handles displaying detailed information about an app with the given package name.
 * @param packageName The package name of the app to show details for.
 * @param onShowSnackbar Called when a snackbar should be shown with the given text.
 * @param modifier The Modifier to apply to the screen.
 */
@Composable
fun AppInfoScreen(
    packageName: String,
    onShowSnackbar: suspend (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AppInfoViewModel = getViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var appDetails: WatchAppDetails?  by remember { mutableStateOf(null) }
    LaunchedEffect(packageName) {
        appDetails = viewModel.getDetailsFor(packageName)
    }

    appDetails?.let { app ->
        if (appDetails != null) {
            AppInfo(
                modifier = modifier,
                app = app,
                onOpenClicked = {
                    coroutineScope.launch {
                        if (viewModel.sendOpenRequest(it)) {
                            onShowSnackbar(context.getString(R.string.appmanager_continue_on_watch))
                        }
                    }
                },
                onUninstallClicked = {
                    coroutineScope.launch {
                        if (viewModel.sendUninstallRequest(it)) {
                            onShowSnackbar(context.getString(R.string.appmanager_continue_on_watch))
                        }
                    }
                }
            )
        }
    }

}
