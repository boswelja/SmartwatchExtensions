package com.boswelja.smartwatchextensions.settings.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.ui.Card
import com.boswelja.smartwatchextensions.common.ui.CardHeader
import com.boswelja.smartwatchextensions.common.ui.DialogSetting
import kotlinx.coroutines.Dispatchers
import org.koin.androidx.compose.getViewModel

/**
 * A Composable for displaying QS Tile settings.
 * @param modifier [Modifier].
 */
@Composable
fun QSTileSettingsCard(
    modifier: Modifier = Modifier
) {
    val viewModel: AppSettingsViewModel = getViewModel()
    val registeredWatches by viewModel.registeredWatches.collectAsState(emptyList(), Dispatchers.IO)
    val qsTilesWatch by viewModel.qsTilesWatch.collectAsState(null, Dispatchers.IO)
    Card(
        modifier = modifier,
        header = {
            CardHeader(title = { Text(stringResource(R.string.category_qstiles)) })
        }
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
