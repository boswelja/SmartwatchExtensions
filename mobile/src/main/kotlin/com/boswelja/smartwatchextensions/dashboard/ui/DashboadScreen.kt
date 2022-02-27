package com.boswelja.smartwatchextensions.dashboard.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.ui.StaggeredVerticalGrid
import org.koin.androidx.compose.getViewModel

/**
 * A screen to display the dashboard.
 * @param modifier [Modifier].
 * @param contentPadding The screen padding.
 * @param onNavigateTo Called when navigation is requested.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp,
    onNavigateTo: (DashboardDestination) -> Unit
) {
    val scrollState = rememberScrollState()
    val viewModel: DashboardViewModel = getViewModel()
    val watchStatus by viewModel.status.collectAsState()
    val selectedWatch by viewModel.selectedWatch.collectAsState()
    val registeredWatches by viewModel.registeredWatches.collectAsState()
    val batteryStats by viewModel.batteryStats.collectAsState()
    val appCount by viewModel.appCount.collectAsState()

    Column(modifier.verticalScroll(scrollState)) {
        StaggeredVerticalGrid(
            modifier = Modifier.padding(contentPadding),
            cells = GridCells.Adaptive(172.dp),
            contentSpacing = contentPadding
        ) {
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
            DashboardItem(
                content = batteryStats?.let { batteryStats ->
                    { BatterySummarySmall(batteryStats = batteryStats) }
                },
                titleText = stringResource(R.string.battery_sync_title),
                onClick = { onNavigateTo(DashboardDestination.BATTERY_SYNC_SETTINGS) }
            )
            DashboardItem(
                content = if (appCount > 0) {
                    { AppSummarySmall(appCount = appCount) }
                } else null,
                titleText = stringResource(R.string.main_app_manager_title),
                onClick = { onNavigateTo(DashboardDestination.APP_MANAGER) }
            )
            DashboardItem(
                titleText = stringResource(com.boswelja.smartwatchextensions.dndsync.R.string.main_dnd_sync_title),
                onClick = { onNavigateTo(DashboardDestination.DND_SYNC_SETTINGS) }
            )
            DashboardItem(
                titleText = stringResource(
                    com.boswelja.smartwatchextensions.phonelocking.R.string.main_phone_locking_title
                ),
                onClick = { onNavigateTo(DashboardDestination.PHONE_LOCKING_SETTINGS) }
            )
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
