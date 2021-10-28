package com.boswelja.smartwatchextensions.dashboard.ui

import androidx.lifecycle.ViewModel
import com.boswelja.smartwatchextensions.appmanager.WatchAppRepository
import com.boswelja.smartwatchextensions.batterysync.BatteryStatsRepository
import com.boswelja.smartwatchextensions.devicemanagement.WatchManager
import com.boswelja.watchconnection.common.discovery.ConnectionMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

/**
 * A ViewModel to provide data used on the dashboard.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    private val batteryStatsRepository: BatteryStatsRepository,
    private val appRepository: WatchAppRepository,
    private val watchManager: WatchManager
) : ViewModel() {

    /**
     * Flow the status of the selected watch.
     */
    val status = watchManager.selectedWatch.flatMapLatest { watch ->
        watch?.let {
            watchManager.getStatusFor(watch)
        } ?: flowOf(ConnectionMode.Disconnected)
    }

    /**
     * Flow battery stats for the selected watch.
     */
    val batteryStats = watchManager.selectedWatch.flatMapLatest { watch ->
        watch?.let {
            batteryStatsRepository.batteryStatsFor(watch.uid)
        } ?: flowOf(null)
    }

    /**
     * Flow the total number of apps installed on the selected watch.
     */
    val appCount = watchManager.selectedWatch.flatMapLatest { watch ->
        watch?.let {
            appRepository.countFor(watch.uid)
        } ?: flow { emit(0L) }
    }
}
