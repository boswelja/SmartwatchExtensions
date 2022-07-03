package com.boswelja.smartwatchextensions.batterysync.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetPhoneBatteryStats
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.RequestBatteryStatsUpdate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

/**
 * A ViewModel to provide data for the Battery Stats UI.
 */
class BatteryStatsViewModel(
    getPhoneBatteryStats: GetPhoneBatteryStats,
    private val requestBatteryStatsUpdate: RequestBatteryStatsUpdate
) : ViewModel() {

    /**
     * Flow the paired phone's battery stats, or null if nothing has been received yet.
     */
    val batteryStats = getPhoneBatteryStats()
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            null
        )

    /**
     * Request updated battery stats from the connected device.
     * @return true if the request was sent successfully, false otherwise.
     */
    suspend fun trySyncBattery(): Boolean {
        return requestBatteryStatsUpdate()
    }
}
