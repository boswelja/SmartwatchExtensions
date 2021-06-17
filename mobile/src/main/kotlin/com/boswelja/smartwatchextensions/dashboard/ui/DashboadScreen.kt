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
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.OpenInFull
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.appmanager.ui.AppManagerActivity
import com.boswelja.smartwatchextensions.appmanager.ui.AppSummarySmall
import com.boswelja.smartwatchextensions.batterysync.ui.BatterySummarySmall
import com.boswelja.smartwatchextensions.batterysync.ui.BatterySyncSettingsActivity
import com.boswelja.smartwatchextensions.common.startActivity
import com.boswelja.smartwatchextensions.common.ui.StaggeredVerticalGrid
import com.boswelja.smartwatchextensions.dndsync.ui.DnDSyncSettingsActivity
import com.boswelja.smartwatchextensions.phonelocking.ui.PhoneLockingSettingsActivity
import com.boswelja.smartwatchextensions.watchmanager.ui.WatchStatusSummarySmall
import com.boswelja.watchconnection.core.discovery.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalFoundationApi
@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val viewModel: DashboardViewModel = viewModel()
    val watchStatus by viewModel.status.collectAsState(Status.CONNECTING, Dispatchers.IO)
    val batteryStats by viewModel.batteryStats.collectAsState(null, Dispatchers.IO)
    val appCount by viewModel.appCount.collectAsState(0, Dispatchers.IO)

    Column(modifier.verticalScroll(scrollState)) {
        StaggeredVerticalGrid(
            modifier = Modifier.padding(16.dp),
            cells = GridCells.Adaptive(172.dp),
            contentSpacing = 16.dp
        ) {
            DashboardItem(
                content = { WatchStatusSummarySmall(watchStatus = watchStatus) }
            )
            DashboardItem(
                content = batteryStats?.let { batteryStats ->
                    { BatterySummarySmall(batteryStats = batteryStats) }
                },
                titleText = stringResource(R.string.battery_sync_title),
                onClick = {
                    context.startActivity<BatterySyncSettingsActivity>()
                }
            )
            DashboardItem(
                content = if (appCount > 0) {
                    { AppSummarySmall(appCount = appCount) }
                } else null,
                titleText = stringResource(R.string.main_app_manager_title),
                onClick = {
                    context.startActivity<AppManagerActivity>()
                }
            )
            DashboardItem(
                titleText = stringResource(R.string.main_dnd_sync_title),
                onClick = {
                    context.startActivity<DnDSyncSettingsActivity>()
                }
            )
            DashboardItem(
                titleText = stringResource(R.string.main_phone_locking_title),
                onClick = {
                    context.startActivity<PhoneLockingSettingsActivity>()
                }
            )
        }
    }
}

@ExperimentalMaterialApi
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
