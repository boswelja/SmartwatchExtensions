package com.boswelja.smartwatchextensions.appsettings.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.ui.DialogSetting
import kotlinx.coroutines.Dispatchers

@Composable
fun QSTileSettingsCard(
    modifier: Modifier = Modifier
) {
    val viewModel: AppSettingsViewModel = viewModel()
    val registeredWatches by viewModel.registeredWatches.collectAsState(emptyList(), Dispatchers.IO)
    val qsTilesWatch by viewModel.qsTilesWatch.collectAsState(null, Dispatchers.IO)
    SettingsCard(
        modifier = modifier,
        title = { Text(stringResource(R.string.category_qstiles)) }
    ) {
        Column {
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
}
