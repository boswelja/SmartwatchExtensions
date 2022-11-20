package com.boswelja.smartwatchextensions.dashboard.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.appmanager.WatchAppRepository
import com.boswelja.smartwatchextensions.batterysync.domain.repository.BatteryStatsRepository
import com.boswelja.smartwatchextensions.core.watches.selected.SelectedWatchController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

/**
 * A ViewModel to provide data used on the dashboard.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    private val batteryStatsRepository: BatteryStatsRepository,
    private val appRepository: WatchAppRepository,
    private val selectedWatchController: SelectedWatchController
) : ViewModel() {

    /**
     * Flow battery stats for the selected watch.
     */
    val batteryStats = mapStateForSelectedWatch(null) {
        batteryStatsRepository.getBatteryStatsForWatch(it)
    }

    /**
     * Flow the total number of apps installed on the selected watch.
     */
    val appCount = mapStateForSelectedWatch(0L) {
        appRepository.countFor(it)
    }

    private fun <T> mapStateForSelectedWatch(
        defaultValue: T,
        block: (String) -> Flow<T>
    ): StateFlow<T> =
        selectedWatchController.selectedWatch
            .filterNotNull()
            .flatMapLatest(block)
            .stateIn(
                viewModelScope,
                SharingStarted.Lazily,
                defaultValue
            )
}
