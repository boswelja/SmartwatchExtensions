package com.boswelja.smartwatchextensions.batterysync.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.batterysync.DefaultValues
import com.boswelja.smartwatchextensions.batterysync.domain.model.DeviceBatteryNotificationState
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetBatteryChargeThreshold
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetBatteryLowThreshold
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetBatteryStats
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetBatterySyncEnabled
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetPhoneBatteryNotificationState
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetWatchBatteryNotificationState
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.SetBatteryChargeThreshold
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.SetBatteryLowThreshold
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.SetBatterySyncEnabled
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * A ViewModel to provide data for Battery Sync
 */
class BatterySyncViewModel(
    getBatterySyncEnabled: GetBatterySyncEnabled,
    getBatteryStats: GetBatteryStats,
    getBatteryChargeThreshold: GetBatteryChargeThreshold,
    getBatteryLowThreshold: GetBatteryLowThreshold,
    getPhoneBatteryNotificationState: GetPhoneBatteryNotificationState,
    getWatchBatteryNotificationState: GetWatchBatteryNotificationState,
    private val setBatterySyncEnabled: SetBatterySyncEnabled,
    private val setBatteryLowThreshold: SetBatteryLowThreshold,
    private val setBatteryChargeThreshold: SetBatteryChargeThreshold
) : ViewModel() {

    /**
     * Flow whether Battery Sync is enabled for the selected watch.
     */
    val batterySyncEnabled = getBatterySyncEnabled()
        .map {
            it.getOrElse {
                null
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            null
        )

    /**
     * Flow the charge percent threshold.
     */
    val chargeThreshold = getBatteryChargeThreshold()
        .map {
            it.getOrElse {
                DefaultValues.CHARGE_THRESHOLD
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            DefaultValues.CHARGE_THRESHOLD
        )

    /**
     * Flow the low percent threshold.
     */
    val batteryLowThreshold = getBatteryLowThreshold()
        .map {
            it.getOrElse {
                DefaultValues.LOW_THRESHOLD
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            DefaultValues.LOW_THRESHOLD
        )

    /**
     * Flow whether watch charge notifications are enabled for the selected watch.
     */
    val watchBatteryNotiState = getWatchBatteryNotificationState()
        .map {
            it.getOrElse {
                DeviceBatteryNotificationState(
                    chargeNotificationsEnabled = false,
                    lowNotificationsEnabled = false
                )
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            DeviceBatteryNotificationState(
                chargeNotificationsEnabled = false,
                lowNotificationsEnabled = false
            )
        )

    /**
     * Flow whether phone charge notifications are enabled for the selected watch.
     */
    val phoneBatteryNotiState = getPhoneBatteryNotificationState()
        .map {
            it.getOrElse {
                DeviceBatteryNotificationState(
                    chargeNotificationsEnabled = false,
                    lowNotificationsEnabled = false
                )
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            DeviceBatteryNotificationState(
                chargeNotificationsEnabled = false,
                lowNotificationsEnabled = false
            )
        )

    /**
     * Flow the stored battery stats for the selected watch.
     */
    val batteryStats = getBatteryStats()
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            null
        )

    /**
     * Set whether Battery Sync is enabled.
     */
    fun setBatterySyncEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            setBatterySyncEnabled.invoke(isEnabled)
        }
    }

    /**
     * Set the charge notification threshold.
     */
    fun setChargeThreshold(chargeThreshold: Int) {
        viewModelScope.launch {
            setBatteryChargeThreshold(chargeThreshold)
        }
    }

    /**
     * Set the low battery notification threshold.
     */
    fun setLowBatteryThreshold(lowThreshold: Int) {
        viewModelScope.launch {
            setBatteryLowThreshold(lowThreshold)
        }
    }
}
