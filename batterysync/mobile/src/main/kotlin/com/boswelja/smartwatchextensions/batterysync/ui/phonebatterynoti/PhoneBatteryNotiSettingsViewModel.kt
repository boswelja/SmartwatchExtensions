package com.boswelja.smartwatchextensions.batterysync.ui.phonebatterynoti

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.batterysync.DefaultValues
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetBatteryChargeThreshold
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetBatteryLowThreshold
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetPhoneChargeNotificationEnabled
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetPhoneLowNotificationEnabled
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.SetPhoneChargeNotificationEnabled
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.SetPhoneLowNotificationEnabled
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * A ViewModel to provide data for phone battery notification settings.
 */
class PhoneBatteryNotiSettingsViewModel(
    getPhoneChargeNotificationEnabled: GetPhoneChargeNotificationEnabled,
    getPhoneLowNotificationEnabled: GetPhoneLowNotificationEnabled,
    getBatteryChargeThreshold: GetBatteryChargeThreshold,
    getBatteryLowThreshold: GetBatteryLowThreshold,
    private val setPhoneChargeNotificationEnabled: SetPhoneChargeNotificationEnabled,
    private val setPhoneLowNotificationEnabled: SetPhoneLowNotificationEnabled
) : ViewModel() {

    /**
     * Flow whether phone charge notifications are enabled for the selected watch.
     */
    val phoneChargeNotiEnabled = getPhoneChargeNotificationEnabled()
        .map { it.getOrDefault(false) }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            false
        )

    /**
     * Flow whether phone low notifications are enabled for the selected watch.
     */
    val phoneLowNotiEnabled = getPhoneLowNotificationEnabled()
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
     * Set whether phone charge notifications are enabled.
     */
    fun setPhoneChargeNotiEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            setPhoneChargeNotificationEnabled(isEnabled)
        }
    }

    /**
     * Set whether phone low notifications are enabled.
     */
    fun setPhoneLowNotiEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            setPhoneLowNotificationEnabled(isEnabled)
        }
    }
}
