package com.boswelja.smartwatchextensions.batterysync.ui.watchbatterynoti

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.batterysync.BatterySyncSettingsKeys.BATTERY_CHARGE_THRESHOLD_KEY
import com.boswelja.smartwatchextensions.batterysync.BatterySyncSettingsKeys.BATTERY_LOW_THRESHOLD_KEY
import com.boswelja.smartwatchextensions.batterysync.BatterySyncSettingsKeys.BATTERY_WATCH_CHARGE_NOTI_KEY
import com.boswelja.smartwatchextensions.batterysync.BatterySyncSettingsKeys.BATTERY_WATCH_LOW_NOTI_KEY
import com.boswelja.smartwatchextensions.batterysync.DefaultValues
import com.boswelja.smartwatchextensions.core.devicemanagement.SelectedWatchManager
import com.boswelja.smartwatchextensions.core.settings.WatchSettingsRepository
import com.boswelja.watchconnection.common.Watch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * A ViewModel to provide data for watch battery notification settings.
 */
class WatchBatteryNotiSettingsViewModel(
    private val selectedWatchManager: SelectedWatchManager,
    private val settingsRepository: WatchSettingsRepository
) : ViewModel() {

    /**
     * Flow whether watch charge notifications are enabled for the selected watch.
     */
    val watchChargeNotiEnabled = mapStateForSelectedWatch(false) {
        settingsRepository.getBoolean(it.uid, BATTERY_WATCH_CHARGE_NOTI_KEY)
    }

    /**
     * Flow whether watch low notifications are enabled for the selected watch.
     */
    val watchLowNotiEnabled = mapStateForSelectedWatch(DefaultValues.NOTIFICATIONS_ENABLED) {
        settingsRepository.getBoolean(it.uid, BATTERY_WATCH_LOW_NOTI_KEY)
    }

    /**
     * Flow the charge percent threshold.
     */
    val chargeThreshold = mapStateForSelectedWatch(DefaultValues.CHARGE_THRESHOLD) {
        settingsRepository.getInt(it.uid, BATTERY_CHARGE_THRESHOLD_KEY, DefaultValues.CHARGE_THRESHOLD)
    }

    /**
     * Flow the low percent threshold.
     */
    val batteryLowThreshold = mapStateForSelectedWatch(DefaultValues.LOW_THRESHOLD) {
        settingsRepository.getInt(it.uid, BATTERY_LOW_THRESHOLD_KEY, DefaultValues.LOW_THRESHOLD)
    }

    /**
     * Set whether watch charge notifications are enabled.
     */
    fun setWatchChargeNotiEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            val selectedWatch = selectedWatchManager.selectedWatch.first()
            updateBoolSetting(selectedWatch!!.uid, BATTERY_WATCH_CHARGE_NOTI_KEY, isEnabled)
        }
    }

    /**
     * Set whether watch low notifications are enabled.
     */
    fun setWatchLowNotiEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            val selectedWatch = selectedWatchManager.selectedWatch.first()
            updateBoolSetting(selectedWatch!!.uid, BATTERY_WATCH_LOW_NOTI_KEY, isEnabled)
        }
    }

    private suspend fun updateBoolSetting(
        watchUid: String,
        key: String,
        value: Boolean
    ) {
        settingsRepository.putBoolean(watchUid, key, value)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
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
