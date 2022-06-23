package com.boswelja.smartwatchextensions.dashboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
    onShowSnackbar: suspend (SnackbarVisuals) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp,
    onNavigateTo: (DashboardDestination) -> Unit
) {
    val viewModel: DashboardViewModel = getViewModel()
    val watchStatus by viewModel.status.collectAsState()
    val selectedWatch by viewModel.selectedWatch.collectAsState()
    val registeredWatches by viewModel.registeredWatches.collectAsState()
    val appCount by viewModel.appCount.collectAsState()

    val itemContentModifier = Modifier.padding(16.dp).fillMaxWidth()
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(contentPadding),
        modifier = modifier
    ) {
        item {
            SelectedWatchSummary(
                selectedWatch = selectedWatch,
                watchStatus = watchStatus,
                registeredWatches = registeredWatches,
                onWatchSelected = viewModel::selectWatchById,
                onShowSnackbar = onShowSnackbar,
                contentModifier = itemContentModifier
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
        item {
            ProximitySummary(
                onClick = { onNavigateTo(DashboardDestination.PROXIMITY_SETTINGS) },
                contentModifier = itemContentModifier
            )
        }
    }
}
