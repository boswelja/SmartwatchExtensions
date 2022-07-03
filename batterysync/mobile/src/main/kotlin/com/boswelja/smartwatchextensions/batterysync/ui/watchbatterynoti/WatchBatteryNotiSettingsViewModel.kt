package com.boswelja.smartwatchextensions.batterysync.ui.watchbatterynoti

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.batterysync.DefaultValues
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetBatteryChargeThreshold
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetBatteryLowThreshold
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetWatchChargeNotificationEnabled
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetWatchLowNotificationEnabled
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.SetWatchChargeNotificationEnabled
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.SetWatchLowNotificationEnabled
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * A ViewModel to provide data for watch battery notification settings.
 */
class WatchBatteryNotiSettingsViewModel(
    getWatchChargeNotificationEnabled: GetWatchChargeNotificationEnabled,
    getWatchLowNotificationEnabled: GetWatchLowNotificationEnabled,
    getBatteryChargeThreshold: GetBatteryChargeThreshold,
    getBatteryLowThreshold: GetBatteryLowThreshold,
    private val setWatchChargeNotificationEnabled: SetWatchChargeNotificationEnabled,
    private val setWatchLowNotificationEnabled: SetWatchLowNotificationEnabled
) : ViewModel() {

    /**
     * Flow whether watch charge notifications are enabled for the selected watch.
     */
    val watchChargeNotiEnabled = getWatchChargeNotificationEnabled()
        .map { it.getOrDefault(false) }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            false
        )

    /**
     * Flow whether watch low notifications are enabled for the selected watch.
     */
    val watchLowNotiEnabled = getWatchLowNotificationEnabled()
        .map { it.getOrDefault(false) }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            false
        )

    /**
     * Flow the charge percent threshold.
     */
    val chargeThreshold = getBatteryChargeThreshold()
        .map { it.getOrDefault(DefaultValues.CHARGE_THRESHOLD) }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            DefaultValues.CHARGE_THRESHOLD
        )

    /**
     * Flow the low percent threshold.
     */
    val batteryLowThreshold = getBatteryLowThreshold()
        .map { it.getOrDefault(DefaultValues.LOW_THRESHOLD) }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            DefaultValues.LOW_THRESHOLD
        )

    /**
     * Set whether watch charge notifications are enabled.
     */
    fun setWatchChargeNotiEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            setWatchChargeNotificationEnabled(isEnabled)
        }
    }

    /**
     * Set whether watch low notifications are enabled.
     */
    fun setWatchLowNotiEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            setWatchLowNotificationEnabled(isEnabled)
        }
    }
}
