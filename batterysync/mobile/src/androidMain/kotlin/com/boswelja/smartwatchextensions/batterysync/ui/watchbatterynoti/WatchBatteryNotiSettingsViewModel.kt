package com.boswelja.smartwatchextensions.batterysync.ui.watchbatterynoti

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.batterysync.DefaultValues
import com.boswelja.smartwatchextensions.devicemanagement.SelectedWatchManager
import com.boswelja.smartwatchextensions.settings.BoolSetting
import com.boswelja.smartwatchextensions.settings.BoolSettingKeys
import com.boswelja.smartwatchextensions.settings.BoolSettingSerializer
import com.boswelja.smartwatchextensions.settings.IntSettingKeys
import com.boswelja.smartwatchextensions.settings.UPDATE_BOOL_PREFERENCE
import com.boswelja.smartwatchextensions.settings.WatchSettingsRepository
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

class WatchBatteryNotiSettingsViewModel(
    messageClient: MessageClient,
    private val selectedWatchManager: SelectedWatchManager,
    private val settingsRepository: WatchSettingsRepository
) : ViewModel() {

    private val boolMessageHandler = MessageHandler(BoolSettingSerializer, messageClient)

    /**
     * Flow whether watch charge notifications are enabled for the selected watch.
     */
    val watchChargeNotiEnabled = mapStateForSelectedWatch(false) {
        settingsRepository.getBoolean(it.uid, BoolSettingKeys.BATTERY_WATCH_CHARGE_NOTI_KEY)
    }

    /**
     * Flow whether watch low notifications are enabled for the selected watch.
     */
    val watchLowNotiEnabled = mapStateForSelectedWatch(DefaultValues.NOTIFICATIONS_ENABLED) {
        settingsRepository.getBoolean(it.uid, BoolSettingKeys.BATTERY_WATCH_LOW_NOTI_KEY)
    }

    /**
     * Flow the charge percent threshold.
     */
    val chargeThreshold = mapStateForSelectedWatch(DefaultValues.CHARGE_THRESHOLD) {
        settingsRepository.getInt(it.uid, IntSettingKeys.BATTERY_CHARGE_THRESHOLD_KEY)
    }

    /**
     * Flow the low percent threshold.
     */
    val batteryLowThreshold = mapStateForSelectedWatch(DefaultValues.LOW_THRESHOLD) {
        settingsRepository.getInt(it.uid, IntSettingKeys.BATTERY_LOW_THRESHOLD_KEY)
    }

    /**
     * Set whether watch charge notifications are enabled.
     */
    fun setWatchChargeNotiEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            val selectedWatch = selectedWatchManager.selectedWatch.first()
            updateBoolSetting(selectedWatch!!.uid, BoolSettingKeys.BATTERY_WATCH_CHARGE_NOTI_KEY, isEnabled)
        }
    }

    /**
     * Set whether watch low notifications are enabled.
     */
    fun setWatchLowNotiEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            val selectedWatch = selectedWatchManager.selectedWatch.first()
            updateBoolSetting(selectedWatch!!.uid, BoolSettingKeys.BATTERY_WATCH_LOW_NOTI_KEY, isEnabled)
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
                UPDATE_BOOL_PREFERENCE,
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
