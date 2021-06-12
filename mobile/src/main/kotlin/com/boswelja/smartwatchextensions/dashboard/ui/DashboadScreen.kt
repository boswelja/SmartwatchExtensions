package com.boswelja.smartwatchextensions.dashboard.ui

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.NavigateNext
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.appmanager.ui.AppManagerActivity
import com.boswelja.smartwatchextensions.batterysync.ui.BatterySyncSettingsActivity
import com.boswelja.smartwatchextensions.batterysync.ui.BatterySyncSettingsHeader
import com.boswelja.smartwatchextensions.common.ui.StaggeredVerticalGrid
import com.boswelja.smartwatchextensions.dndsync.ui.DnDSyncSettingsActivity
import com.boswelja.smartwatchextensions.phonelocking.ui.PhoneLockingSettingsActivity
import com.boswelja.watchconnection.core.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber

@ExperimentalFoundationApi
@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@Composable
fun DashboardScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val viewModel: DashboardViewModel = viewModel()
    val watchStatus by viewModel.status.collectAsState(Status.CONNECTING, Dispatchers.IO)

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        WatchStatus(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp),
            status = watchStatus
        )
        StaggeredVerticalGrid(
            modifier = Modifier.padding(8.dp),
            cells = GridCells.Fixed(2)
        ) {
            DashboardItem(
                content = { BatterySyncSettingsHeader() },
                buttonLabel = stringResource(
                    R.string.dashboard_settings_label,
                    stringResource(R.string.battery_sync_title)
                ),
                onClick = {
                    context.startActivity(Intent(context, BatterySyncSettingsActivity::class.java))
                }
            )
            DashboardItem(
                content = { },
                buttonLabel = stringResource(
                    R.string.dashboard_settings_label,
                    stringResource(R.string.main_dnd_sync_title)
                ),
                onClick = {
                    context.startActivity(Intent(context, DnDSyncSettingsActivity::class.java))
                }
            )
            DashboardItem(
                content = { },
                buttonLabel = stringResource(
                    R.string.dashboard_settings_label,
                    stringResource(R.string.main_phone_locking_title)
                ),
                onClick = {
                    context.startActivity(Intent(context, PhoneLockingSettingsActivity::class.java))
                }
            )
            DashboardItem(
                content = { },
                buttonLabel = stringResource(R.string.main_app_manager_title),
                onClick = {
                    context.startActivity(Intent(context, AppManagerActivity::class.java))
                }
            )
        }
    }
}

@Composable
fun WatchStatus(
    modifier: Modifier = Modifier,
    status: Status?
) {
    Timber.d("Selected watch status = %s", status)
    val (icon, label) = when (status) {
        Status.CONNECTING ->
            Pair(Icons.Outlined.Sync, stringResource(R.string.watch_status_connecting))
        Status.CONNECTED ->
            Pair(
                Icons.Outlined.CheckCircleOutline,
                stringResource(R.string.watch_status_connected)
            )
        Status.DISCONNECTED ->
            Pair(
                Icons.Outlined.ErrorOutline,
                stringResource(R.string.watch_status_disconnected)
            )
        Status.MISSING_APP ->
            Pair(
                Icons.Outlined.ErrorOutline,
                stringResource(R.string.watch_status_missing_app)
            )
        else ->
            Pair(
                Icons.Outlined.ErrorOutline,
                stringResource(R.string.watch_status_error)
            )
    }
    Card(modifier) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, null)
            Text(label, modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@ExperimentalMaterialApi
@Composable
fun DashboardItem(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
    buttonLabel: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.padding(8.dp),
        onClick = onClick
    ) {
        Column(Modifier.fillMaxSize()) {
            content()
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    buttonLabel,
                    style = MaterialTheme.typography.button,
                    color = MaterialTheme.colors.primary
                )
                Icon(
                    Icons.Outlined.NavigateNext,
                    null,
                    tint = MaterialTheme.colors.primary
                )
            }
        }
    }
}
