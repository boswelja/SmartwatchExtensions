package com.boswelja.smartwatchextensions.batterysync.ui.phonebatterynoti

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.batterysync.BatterySyncSettingsKeys.BATTERY_PHONE_CHARGE_NOTI_KEY
import com.boswelja.smartwatchextensions.batterysync.BatterySyncSettingsKeys.BATTERY_PHONE_LOW_NOTI_KEY
import com.boswelja.smartwatchextensions.batterysync.DefaultValues
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetBatteryChargeThreshold
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetBatteryLowThreshold
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetPhoneChargeNotificationEnabled
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetPhoneLowNotificationEnabled
import com.boswelja.smartwatchextensions.core.devicemanagement.SelectedWatchManager
import com.boswelja.smartwatchextensions.core.settings.BoolSetting
import com.boswelja.smartwatchextensions.core.settings.BoolSettingSerializer
import com.boswelja.smartwatchextensions.core.settings.UpdateBoolSetting
import com.boswelja.smartwatchextensions.core.settings.WatchSettingsRepository
import com.boswelja.watchconnection.common.message.Message
import com.boswelja.watchconnection.core.message.MessageClient
import com.boswelja.watchconnection.serialization.MessageHandler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * A ViewModel to provide data for phone battery notification settings.
 */
class PhoneBatteryNotiSettingsViewModel(
    messageClient: MessageClient,
    private val selectedWatchManager: SelectedWatchManager,
    private val settingsRepository: WatchSettingsRepository,
    getPhoneChargeNotificationEnabled: GetPhoneChargeNotificationEnabled,
    getPhoneLowNotificationEnabled: GetPhoneLowNotificationEnabled,
    getBatteryChargeThreshold: GetBatteryChargeThreshold,
    getBatteryLowThreshold: GetBatteryLowThreshold
) : ViewModel() {
    private val boolMessageHandler = MessageHandler(BoolSettingSerializer, messageClient)

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
            val selectedWatch = selectedWatchManager.selectedWatch.first()
            updateBoolSetting(selectedWatch!!.uid, BATTERY_PHONE_CHARGE_NOTI_KEY, isEnabled)
        }
    }

    /**
     * Set whether phone low notifications are enabled.
     */
    fun setPhoneLowNotiEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            val selectedWatch = selectedWatchManager.selectedWatch.first()
            updateBoolSetting(selectedWatch!!.uid, BATTERY_PHONE_LOW_NOTI_KEY, isEnabled)
        }
    }

    private suspend fun updateBoolSetting(
        watchUid: String,
        key: String,
        value: Boolean
    ) {
        settingsRepository.putBoolean(watchUid, key, value)
        boolMessageHandler.sendMessage(
            watchUid,
            Message(
                UpdateBoolSetting,
                BoolSetting(key, value)
            )
        )
    }
}
