package com.boswelja.smartwatchextensions.dashboard.ui

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.NavigateNext
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import com.boswelja.smartwatchextensions.dndsync.ui.DnDSyncSettingsActivity
import com.boswelja.smartwatchextensions.phonelocking.ui.PhoneLockingSettingsActivity
import com.boswelja.watchconnection.core.Status
import timber.log.Timber

@Composable
fun DashboardScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val viewModel: DashboardViewModel = viewModel()
    val watchStatus by viewModel.status.observeAsState()

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        WatchStatus(watchStatus)
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

@Composable
fun WatchStatus(status: Status?) {
    Timber.d("Selected watch status = %s", status)
    Card {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            when (status) {
                Status.CONNECTING -> {
                    Icon(Icons.Outlined.Sync, null)
                    Text(
                        stringResource(R.string.watch_status_connecting),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Status.CONNECTED -> {
                    Icon(Icons.Outlined.CheckCircleOutline, null)
                    Text(
                        stringResource(R.string.watch_status_connected),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Status.DISCONNECTED -> {
                    Icon(Icons.Outlined.ErrorOutline, null)
                    Text(
                        stringResource(R.string.watch_status_disconnected),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Status.MISSING_APP -> {
                    Icon(Icons.Outlined.ErrorOutline, null)
                    Text(
                        stringResource(R.string.watch_status_missing_app),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Status.ERROR -> {
                    Icon(Icons.Outlined.ErrorOutline, null)
                    Text(
                        stringResource(R.string.watch_status_error),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardItem(
    content: @Composable () -> Unit,
    buttonLabel: String,
    onClick: () -> Unit
) {
    Card {
        Column(
            Modifier.clickable(onClick = onClick)
        ) {
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
