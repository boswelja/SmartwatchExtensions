package com.boswelja.smartwatchextensions.dashboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.boswelja.smartwatchextensions.watchmanager.ui.pick.WatchPicker
import org.koin.compose.viewmodel.koinViewModel

/**
 * A screen to display the dashboard.
 * @param modifier [Modifier].
 * @param onNavigateTo Called when navigation is requested.
 */
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    onNavigateTo: (DashboardDestination) -> Unit
) {
    val viewModel: DashboardViewModel = koinViewModel()
    val appCount by viewModel.appCount.collectAsState()

    val itemModifier = Modifier.padding(horizontal = 16.dp)
    val itemContentModifier = Modifier
        .padding(16.dp)
        .fillMaxWidth()
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        modifier = modifier
    ) {
        item {
            WatchPicker(
                onRegisterNewWatch = {
                    onNavigateTo(DashboardDestination.REGISTER_WATCH)
                },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp)
            )
        }
        item {
            val batteryStats by viewModel.batteryStats.collectAsState()
            if (batteryStats == null) {
                BatterySyncDisabledSummary(
                    onClick = { onNavigateTo(DashboardDestination.BATTERY_SYNC_SETTINGS) },
                    modifier = itemModifier,
                    contentModifier = itemContentModifier
                )
            } else {
                BatterySyncSummary(
                    batteryStats = batteryStats!!,
                    onClick = { onNavigateTo(DashboardDestination.BATTERY_SYNC_SETTINGS) },
                    modifier = itemModifier,
                    contentModifier = itemContentModifier
                )
            }
        }
        item {
            AppManagerSummary(
                userAppCount = appCount,
                onClick = { onNavigateTo(DashboardDestination.APP_MANAGER) },
                modifier = itemModifier,
                contentModifier = itemContentModifier
            )
        }
        item {
            DnDSyncSummary(
                onClick = { onNavigateTo(DashboardDestination.DND_SYNC_SETTINGS) },
                modifier = itemModifier,
                contentModifier = itemContentModifier
            )
        }
        item {
            PhoneLockingSummary(
                onClick = { onNavigateTo(DashboardDestination.PHONE_LOCKING_SETTINGS) },
                modifier = itemModifier,
                contentModifier = itemContentModifier
            )
        }
    }
}
