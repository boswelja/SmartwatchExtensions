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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.boswelja.smartwatchextensions.watchmanager.ui.pick.WatchPicker
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
    val appCount by viewModel.appCount.collectAsState()

    val itemContentModifier = Modifier
        .padding(16.dp)
        .fillMaxWidth()
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(contentPadding),
        modifier = modifier
    ) {
        item {
            WatchPicker(
                onRegisterNewWatch = { /*TODO*/ },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(contentPadding)
            )
        }
        item {
            val batteryStats by viewModel.batteryStats.collectAsState()
            if (batteryStats == null) {
                BatterySyncDisabledSummary(
                    onClick = { onNavigateTo(DashboardDestination.BATTERY_SYNC_SETTINGS) },
                    contentModifier = itemContentModifier
                )
            } else {
                BatterySyncSummary(
                    batteryStats = batteryStats!!,
                    onClick = { onNavigateTo(DashboardDestination.BATTERY_SYNC_SETTINGS) },
                    contentModifier = itemContentModifier
                )
            }
        }
        item {
            AppManagerSummary(
                userAppCount = appCount,
                onClick = { onNavigateTo(DashboardDestination.APP_MANAGER) },
                contentModifier = itemContentModifier
            )
        }
        item {
            DnDSyncSummary(
                onClick = { onNavigateTo(DashboardDestination.DND_SYNC_SETTINGS) },
                contentModifier = itemContentModifier
            )
        }
        item {
            PhoneLockingSummary(
                onClick = { onNavigateTo(DashboardDestination.PHONE_LOCKING_SETTINGS) },
                contentModifier = itemContentModifier
            )
        }
    }
}
