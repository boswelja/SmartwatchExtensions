package com.boswelja.smartwatchextensions.dashboard.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.OpenInFull
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.appmanager.ui.AppSummarySmall
import com.boswelja.smartwatchextensions.batterysync.ui.BatterySummarySmall
import com.boswelja.smartwatchextensions.common.ui.StaggeredVerticalGrid
import com.boswelja.smartwatchextensions.watchmanager.ui.WatchStatusSummarySmall
import com.boswelja.watchconnection.common.discovery.ConnectionMode
import kotlinx.coroutines.Dispatchers

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp,
    onNavigateTo: (DashboardDestination) -> Unit
) {
    val scrollState = rememberScrollState()
    val viewModel: DashboardViewModel = viewModel()
    val watchStatus by viewModel.status.collectAsState(ConnectionMode.Disconnected, Dispatchers.IO)
    val batteryStats by viewModel.batteryStats.collectAsState(null, Dispatchers.IO)
    val appCount by viewModel.appCount.collectAsState(0, Dispatchers.IO)

    Column(modifier.verticalScroll(scrollState)) {
        StaggeredVerticalGrid(
            modifier = Modifier.padding(contentPadding),
            cells = GridCells.Adaptive(172.dp),
            contentSpacing = contentPadding
        ) {
            DashboardItem(
                content = { WatchStatusSummarySmall(watchStatus = watchStatus) }
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
                titleText = stringResource(R.string.main_dnd_sync_title),
                onClick = { onNavigateTo(DashboardDestination.DND_SYNC_SETTINGS) }
            )
            DashboardItem(
                titleText = stringResource(R.string.main_phone_locking_title),
                onClick = { onNavigateTo(DashboardDestination.PHONE_LOCKING_SETTINGS) }
            )
            DashboardItem(
                titleText = stringResource(R.string.proximity_settings_title),
                onClick = { onNavigateTo(DashboardDestination.PROXIMITY_SETTINGS) }
            )
        }
    }
}

@Composable
fun DashboardItem(
    modifier: Modifier = Modifier,
    content: (@Composable () -> Unit)? = null,
    titleText: String? = null,
    onClick: (() -> Unit)? = null
) {
    Card(modifier) {
        Column(
            Modifier
                .fillMaxSize()
                .clickable(enabled = onClick != null) { onClick?.invoke() }
                .padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                titleText?.let {
                    Text(
                        text = titleText,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (onClick != null) {
                    Icon(
                        imageVector = Icons.Outlined.OpenInFull,
                        contentDescription = null,
                        tint = MaterialTheme.colors.onSurface
                    )
                }
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
