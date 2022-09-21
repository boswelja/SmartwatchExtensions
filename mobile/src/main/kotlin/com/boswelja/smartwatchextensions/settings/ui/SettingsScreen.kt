package com.boswelja.smartwatchextensions.settings.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChangeHistory
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.boswelja.smartwatchextensions.BuildConfig
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.startActivity
import com.boswelja.smartwatchextensions.core.ui.list.ListItem
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
    contentPadding: PaddingValues = PaddingValues(),
    onNavigateTo: (SettingsDestination) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding
    ) {
        item {
            AboutAppHeader(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .fillMaxWidth()
            )
        }
        item {
            QSTileSettings()
        }
        item {
            WatchSettings(onNavigateTo = onNavigateTo)
        }
        item {
            AboutAppListShortcuts(onNavigateTo = onNavigateTo)
        }
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

/**
 * Displays information about the app as it's currently installed.
 */
@Composable
fun AboutAppHeader(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Watch,
            contentDescription = null,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = BuildConfig.VERSION_NAME,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

/**
 * Displays a list of shortcuts to app-related information
 */
@Composable
fun AboutAppListShortcuts(
    onNavigateTo: (SettingsDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Column(modifier) {
        ListItem(
            icon = { Icon(Icons.Default.Code, null) },
            text = { Text(stringResource(R.string.about_github)) },
            modifier = Modifier.clickable { onNavigateTo(SettingsDestination.SOURCE) }
        )
        ListItem(
            icon = { Icon(Icons.Default.ChangeHistory, null) },
            text = { Text(stringResource(R.string.about_changelog)) },
            modifier = Modifier.clickable { onNavigateTo(SettingsDestination.CHANGELOG) }
        )
        ListItem(
            icon = { Icon(Icons.Default.Security, null) },
            text = { Text(stringResource(R.string.about_priv_policy_title)) },
            modifier = Modifier.clickable { onNavigateTo(SettingsDestination.PRIVACY_POLICY) }
        )
        ListItem(
            icon = { Icon(Icons.Default.Notifications, null) },
            text = { Text(stringResource(R.string.noti_settings_title)) },
            modifier = Modifier.clickable {
                context.startActivity {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    it.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    it.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName!!)
                }
            }
        )
    }
}
