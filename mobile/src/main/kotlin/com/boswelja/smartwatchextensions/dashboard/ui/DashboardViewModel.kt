package com.boswelja.smartwatchextensions.dashboard.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.appmanager.WatchAppRepository
import com.boswelja.smartwatchextensions.batterysync.domain.repository.BatteryStatsRepository
import com.boswelja.smartwatchextensions.core.devicemanagement.SelectedWatchManager
import com.boswelja.smartwatchextensions.core.devicemanagement.WatchRepository
import com.boswelja.watchconnection.common.Watch
import com.boswelja.watchconnection.common.discovery.ConnectionMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * A ViewModel to provide data used on the dashboard.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    private val batteryStatsRepository: BatteryStatsRepository,
    private val appRepository: WatchAppRepository,
    private val watchRepository: WatchRepository,
    private val selectedWatchManager: SelectedWatchManager
) : ViewModel() {

    /**
     * Flows the current selected watch, or null if no watch is selected.
     */
    val selectedWatch = selectedWatchManager.selectedWatch
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            null
        )

    /**
     * Flows the currently registered watches.
     */
    val registeredWatches = watchRepository.registeredWatches
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            emptyList()
        )

    /**
     * Flow the status of the selected watch.
     */
    val status = mapStateForSelectedWatch(ConnectionMode.Disconnected) {
        watchRepository.getStatusFor(it)
    }

    /**
     * Flow battery stats for the selected watch.
     */
    val batteryStats = mapStateForSelectedWatch(null) {
        batteryStatsRepository.batteryStatsFor(it.uid)
    }

    /**
     * Flow the total number of apps installed on the selected watch.
     */
    val appCount = mapStateForSelectedWatch(0L) {
        appRepository.countFor(it.uid)
    }

    /**
     * Select a watch by it's ID.
     */
    fun selectWatchById(watchId: String) {
        viewModelScope.launch {
            selectedWatchManager.selectWatch(watchId)
        }
    }

    private fun <T> mapStateForSelectedWatch(
        defaultValue: T,
        block: (Watch) -> Flow<T>
    ): StateFlow<T> =
        selectedWatchManager.selectedWatch
            .filterNotNull()
            .flatMapLatest(block)
            .stateIn(
                viewModelScope,
                SharingStarted.Lazily,
                defaultValue
            )
}
