package com.boswelja.smartwatchextensions.settings.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.core.ui.settings.DialogSetting
import com.boswelja.smartwatchextensions.core.ui.settings.SettingsHeader
import com.boswelja.smartwatchextensions.core.ui.settings.ShortcutSetting
import kotlinx.coroutines.Dispatchers
import org.koin.androidx.compose.getViewModel

/**
 * A Composable screen for displaying app settings.
 * @param modifier [Modifier].
 * @param contentPadding The screen padding.
 * @param onNavigateTo Called when navigation is requested.
 */
@Composable
fun AppSettingsScreen(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp,
    onNavigateTo: (SettingsDestination) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(contentPadding),
        verticalArrangement = Arrangement.spacedBy(contentPadding)
    ) {
        item {
            AppSettings()
        }
        item {
            QSTileSettings()
        }
        item {
            WatchSettings(onNavigateTo = onNavigateTo)
        }
    }
}

/**
 * A Composable for displaying app settings.
 * @param modifier [Modifier].
 */
@Composable
fun AppSettings(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Column(modifier) {
        SettingsHeader(
            text = {
                Text(stringResource(R.string.app_settings_title))
            }
        )
        ShortcutSetting(
            text = { Text(stringResource(R.string.noti_settings_title)) },
            onClick = {
                Intent()
                    .apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName!!)
                    }
                    .also {
                        context.startActivity(it)
                    }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * A Composable for displaying QS Tile settings.
 * @param modifier [Modifier].
 */
@Composable
fun QSTileSettings(
    modifier: Modifier = Modifier
) {
    val viewModel: AppSettingsViewModel = getViewModel()
    val registeredWatches by viewModel.registeredWatches.collectAsState(emptyList(), Dispatchers.IO)
    val qsTilesWatch by viewModel.qsTilesWatch.collectAsState(null, Dispatchers.IO)

    Column(modifier) {
        SettingsHeader(
            text = {
                Text(stringResource(R.string.category_qstiles))
            }
        )
        DialogSetting(
            icon = { Icon(Icons.Outlined.Watch, null) },
            label = { Text(stringResource(R.string.qstiles_selected_watch)) },
            summary = { Text(qsTilesWatch?.name ?: "") },
            values = registeredWatches,
            value = qsTilesWatch,
            onValueChanged = {
                viewModel.setQSTilesWatch(it!!)
            },
            valueLabel = {
                Text(it?.name ?: stringResource(R.string.watch_status_error))
            }
        )
    }
}

/**
 * A Composable for displaying watch settings.
 * @param modifier [Modifier].
 * @param onNavigateTo Called when navigation is requested.
 */
@Composable
fun WatchSettings(
    modifier: Modifier = Modifier,
    onNavigateTo: (SettingsDestination) -> Unit
) {
    Column(modifier) {
        SettingsHeader(
            text = {
                Text(stringResource(R.string.category_watch_settings))
            }
        )
        ShortcutSetting(
            text = { Text(stringResource(R.string.manage_watches_title)) },
            onClick = { onNavigateTo(SettingsDestination.WATCH_MANAGER) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
