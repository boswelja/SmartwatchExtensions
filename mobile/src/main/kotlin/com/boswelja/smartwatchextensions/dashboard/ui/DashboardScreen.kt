package com.boswelja.smartwatchextensions.dashboard.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.getViewModel

/**
 * A screen to display the dashboard.
 * @param modifier [Modifier].
 * @param contentPadding The screen padding.
 * @param onNavigateTo Called when navigation is requested.
 */
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp,
    onNavigateTo: (DashboardDestination) -> Unit
) {
    val viewModel: DashboardViewModel = getViewModel()
    val watchStatus by viewModel.status.collectAsState()
    val selectedWatch by viewModel.selectedWatch.collectAsState()
    val registeredWatches by viewModel.registeredWatches.collectAsState()
    val batteryStats by viewModel.batteryStats.collectAsState()
    val appCount by viewModel.appCount.collectAsState()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(contentPadding),
        contentPadding = PaddingValues(contentPadding),
        modifier = modifier
    ) {
        item {
            DashboardItem(
                content = {
                    SelectedWatchSummary(
                        selectedWatch = selectedWatch,
                        watchStatus = watchStatus,
                        registeredWatches = registeredWatches,
                        onWatchSelected = viewModel::selectWatchById
                    )
                }
            )
        }
        item {
            if (batteryStats == null) {
                BatterySyncLoadingSummary(
                    onClick = { onNavigateTo(DashboardDestination.BATTERY_SYNC_SETTINGS) },
                    contentModifier = Modifier.padding(16.dp)
                )
            }
            batteryStats?.let {
                BatterySyncSummary(
                    batteryStats = it,
                    onClick = { onNavigateTo(DashboardDestination.BATTERY_SYNC_SETTINGS) },
                    contentModifier = Modifier.padding(16.dp)
                )
            }
        }
        item {
            AppManagerSummary(
                userAppCount = appCount,
                onClick = { onNavigateTo(DashboardDestination.APP_MANAGER) },
                contentModifier = Modifier.padding(16.dp)
            )
        }
        item {
            DashboardItem(
                titleText = stringResource(com.boswelja.smartwatchextensions.dndsync.R.string.main_dnd_sync_title),
                onClick = { onNavigateTo(DashboardDestination.DND_SYNC_SETTINGS) }
            )
        }
        item {
            DashboardItem(
                titleText = stringResource(
                    com.boswelja.smartwatchextensions.phonelocking.R.string.main_phone_locking_title
                ),
                onClick = { onNavigateTo(DashboardDestination.PHONE_LOCKING_SETTINGS) }
            )
        }
        item {
            DashboardItem(
                titleText = stringResource(
                    com.boswelja.smartwatchextensions.proximity.R.string.proximity_settings_title
                ),
                onClick = { onNavigateTo(DashboardDestination.PROXIMITY_SETTINGS) }
            )
        }
    }
}

/**
 * A Card to display an item on the dashboard.
 * @param modifier [Modifier].
 * @param content The card content.
 * @param titleText The title of the card.
 * @param onClick Called when the card is clicked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardItem(
    modifier: Modifier = Modifier,
    content: (@Composable () -> Unit)? = null,
    titleText: String? = null,
    onClick: (() -> Unit)? = null
) {
    Card(modifier) {
        Column(
            modifier
                .fillMaxWidth()
                .clickable(enabled = onClick != null) { onClick?.invoke() }
                .padding(16.dp)
        ) {
            titleText?.let {
                Text(titleText)
            }
            content?.let {
                if (titleText != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
                content()
            }
        }
    }
}
