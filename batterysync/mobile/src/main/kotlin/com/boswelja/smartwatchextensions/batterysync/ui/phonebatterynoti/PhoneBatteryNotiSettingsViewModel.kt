package com.boswelja.smartwatchextensions.batterysync.ui.phonebatterynoti

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.batterysync.BatterySyncSettingsKeys.BATTERY_CHARGE_THRESHOLD_KEY
import com.boswelja.smartwatchextensions.batterysync.BatterySyncSettingsKeys.BATTERY_LOW_THRESHOLD_KEY
import com.boswelja.smartwatchextensions.batterysync.BatterySyncSettingsKeys.BATTERY_PHONE_CHARGE_NOTI_KEY
import com.boswelja.smartwatchextensions.batterysync.BatterySyncSettingsKeys.BATTERY_PHONE_LOW_NOTI_KEY
import com.boswelja.smartwatchextensions.batterysync.DefaultValues
import com.boswelja.smartwatchextensions.core.devicemanagement.SelectedWatchManager
import com.boswelja.smartwatchextensions.core.settings.BoolSetting
import com.boswelja.smartwatchextensions.core.settings.BoolSettingSerializer
import com.boswelja.smartwatchextensions.core.settings.UpdateBoolSetting
import com.boswelja.smartwatchextensions.core.settings.WatchSettingsRepository
import com.boswelja.watchconnection.common.Watch
import com.boswelja.watchconnection.common.message.Message
import com.boswelja.watchconnection.core.message.MessageClient
import com.boswelja.watchconnection.serialization.MessageHandler
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
 * A ViewModel to provide data for phone battery notification settings.
 */
class PhoneBatteryNotiSettingsViewModel(
    messageClient: MessageClient,
    private val selectedWatchManager: SelectedWatchManager,
    private val settingsRepository: WatchSettingsRepository
) : ViewModel() {
    private val boolMessageHandler = MessageHandler(BoolSettingSerializer, messageClient)

    /**
     * Flow whether phone charge notifications are enabled for the selected watch.
     */
    val phoneChargeNotiEnabled = mapStateForSelectedWatch(false) {
        settingsRepository.getBoolean(it.uid, BATTERY_PHONE_CHARGE_NOTI_KEY)
    }

    /**
     * Flow whether phone low notifications are enabled for the selected watch.
     */
    val phoneLowNotiEnabled = mapStateForSelectedWatch(DefaultValues.NOTIFICATIONS_ENABLED) {
        settingsRepository.getBoolean(it.uid, BATTERY_PHONE_LOW_NOTI_KEY)
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
