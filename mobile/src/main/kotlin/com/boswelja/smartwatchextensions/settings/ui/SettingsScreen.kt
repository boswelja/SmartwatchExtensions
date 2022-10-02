package com.boswelja.smartwatchextensions.settings.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChangeHistory
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.startActivity
import com.boswelja.smartwatchextensions.core.ui.settings.DialogSetting
import kotlinx.coroutines.Dispatchers
import org.koin.androidx.compose.getViewModel

/**
 * A Composable screen for displaying app settings.
 * @param onNavigateTo Called when navigation is requested.
 * @param modifier [Modifier].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSettingsScreen(
    onNavigateTo: (SettingsDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: AppSettingsViewModel = getViewModel()
    val registeredWatches by viewModel.registeredWatches.collectAsState(emptyList(), Dispatchers.IO)
    val qsTilesWatch by viewModel.qsTilesWatch.collectAsState(null, Dispatchers.IO)

    val itemModifier = Modifier.fillMaxWidth()
    LazyColumn(modifier = modifier) {
        item {
            AboutAppHeader(
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
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
                },
                modifier = itemModifier
            )
        }
        item {
            ListItem(
                headlineText = { Text(stringResource(R.string.manage_watches_title)) },
                modifier = Modifier
                    .clickable {
                        onNavigateTo(SettingsDestination.WATCH_MANAGER)
                    }.then(itemModifier)
            )
        }
        aboutAppListShortcuts(
            onNavigateTo = onNavigateTo,
            modifier = itemModifier
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
    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter
    ) {
        Icon(
            imageVector = Icons.Default.Watch,
            contentDescription = null,
            modifier = Modifier.size(64.dp)
        )
    }
}

/**
 * Displays a list of shortcuts to app-related information
 */
@OptIn(ExperimentalMaterial3Api::class)
fun LazyListScope.aboutAppListShortcuts(
    onNavigateTo: (SettingsDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    item {
        val context = LocalContext.current
        ListItem(
            headlineText = { Text(stringResource(R.string.noti_settings_title)) },
            leadingContent = { Icon(Icons.Default.Notifications, null) },
            modifier = Modifier.clickable {
                context.startActivity {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    it.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    it.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName!!)
                }
            }.then(modifier)
        )
    }
    item {
        ListItem(
            headlineText = { Text(stringResource(R.string.about_github)) },
            leadingContent = { Icon(Icons.Default.Code, null) },
            modifier = Modifier.clickable { onNavigateTo(SettingsDestination.SOURCE) }.then(modifier)
        )
    }
    item {
        ListItem(
            headlineText = { Text(stringResource(R.string.about_changelog)) },
            leadingContent = { Icon(Icons.Default.ChangeHistory, null) },
            modifier = Modifier.clickable { onNavigateTo(SettingsDestination.CHANGELOG) }.then(modifier)
        )
    }
    item {
        ListItem(
            headlineText = { Text(stringResource(R.string.about_priv_policy_title)) },
            leadingContent = { Icon(Icons.Default.Security, null) },
            modifier = Modifier.clickable { onNavigateTo(SettingsDestination.PRIVACY_POLICY) }.then(modifier)
        )
    }
}
