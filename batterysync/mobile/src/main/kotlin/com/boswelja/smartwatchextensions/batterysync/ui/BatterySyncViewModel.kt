package com.boswelja.smartwatchextensions.batterysync.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.batterysync.BatterySyncSettingsKeys.BATTERY_CHARGE_THRESHOLD_KEY
import com.boswelja.smartwatchextensions.batterysync.BatterySyncSettingsKeys.BATTERY_LOW_THRESHOLD_KEY
import com.boswelja.smartwatchextensions.batterysync.BatterySyncSettingsKeys.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.smartwatchextensions.batterysync.platform.BatterySyncWorker
import com.boswelja.smartwatchextensions.batterysync.DefaultValues
import com.boswelja.smartwatchextensions.batterysync.SyncBatteryStatus
import com.boswelja.smartwatchextensions.batterysync.domain.model.DeviceBatteryNotificationState
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetBatteryChargeThreshold
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetBatteryLowThreshold
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetBatteryStats
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetBatterySyncEnabled
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetPhoneBatteryNotificationState
import com.boswelja.smartwatchextensions.batterysync.domain.usecase.GetWatchBatteryNotificationState
import com.boswelja.smartwatchextensions.core.devicemanagement.SelectedWatchManager
import com.boswelja.smartwatchextensions.core.settings.BoolSetting
import com.boswelja.smartwatchextensions.core.settings.BoolSettingSerializer
import com.boswelja.smartwatchextensions.core.settings.IntSettingSerializer
import com.boswelja.smartwatchextensions.core.settings.UpdateBoolSetting
import com.boswelja.smartwatchextensions.core.settings.UpdateIntSetting
import com.boswelja.smartwatchextensions.core.settings.WatchSettingsRepository
import com.boswelja.smartwatchextensions.core.settings.IntSetting
import com.boswelja.watchconnection.common.Watch
import com.boswelja.watchconnection.common.message.Message
import com.boswelja.watchconnection.core.discovery.DiscoveryClient
import com.boswelja.watchconnection.core.message.MessageClient
import com.boswelja.watchconnection.serialization.MessageHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * A ViewModel to provide data for Battery Sync
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BatterySyncViewModel(
    application: Application,
    messageClient: MessageClient,
    discoveryClient: DiscoveryClient,
    private val selectedWatchManager: SelectedWatchManager,
    private val settingsRepository: WatchSettingsRepository,
    getBatterySyncEnabled: GetBatterySyncEnabled,
    getBatteryStats: GetBatteryStats,
    getBatteryChargeThreshold: GetBatteryChargeThreshold,
    getBatteryLowThreshold: GetBatteryLowThreshold,
    getPhoneBatteryNotificationState: GetPhoneBatteryNotificationState,
    getWatchBatteryNotificationState: GetWatchBatteryNotificationState
) : AndroidViewModel(application) {

    private val boolMessageHandler = MessageHandler(BoolSettingSerializer, messageClient)
    private val intMessageHandler = MessageHandler(IntSettingSerializer, messageClient)

    /**
     * Flow whether Battery Sync is enabled for the selected watch.
     */
    val batterySyncEnabled = getBatterySyncEnabled()
        .map {
            it.getOrElse {
                _isError.value = true
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
                _isError.value = true
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
                _isError.value = true
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
                _isError.value = true
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
                _isError.value = true
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
     * Flow whether the selected watch supports Battery Sync.
     */
    val canSyncBattery = mapStateForSelectedWatch(false) {
        discoveryClient.hasCapability(it.uid, SyncBatteryStatus)
    }

    /**
     * Flow the stored battery stats for the selected watch.
     */
    val batteryStats = getBatteryStats()
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            null
        )

    private val _isError = MutableStateFlow(false)
    val isError: StateFlow<Boolean> = _isError

    /**
     * Set whether Battery Sync is enabled.
     */
    fun setBatterySyncEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            val selectedWatch = selectedWatchManager.selectedWatch.first()
            if (isEnabled) {
                val workerStartSuccessful = BatterySyncWorker
                    .startSyncingFor(getApplication(), selectedWatch!!.uid)
                if (workerStartSuccessful) {
                    updateBoolSetting(
                        selectedWatch.uid,
                        BATTERY_SYNC_ENABLED_KEY,
                        isEnabled
                    )
                }
            } else {
                updateBoolSetting(
                    selectedWatch!!.uid,
                    BATTERY_SYNC_ENABLED_KEY,
                    isEnabled
                )
                BatterySyncWorker.stopSyncingFor(
                    getApplication(), selectedWatch.uid
                )
                // TODO reenable this
//                WatchBatteryTileService.requestTileUpdate(getApplication())
//                WatchWidgetProvider.updateWidgets(getApplication())
            }
        }
    }

    /**
     * Set the charge notification threshold.
     */
    fun setChargeThreshold(chargeThreshold: Int) {
        viewModelScope.launch {
            val selectedWatch = selectedWatchManager.selectedWatch.first()
            updateIntSetting(selectedWatch!!.uid, BATTERY_CHARGE_THRESHOLD_KEY, chargeThreshold)
        }
    }

    /**
     * Set the low battery notification threshold.
     */
    fun setLowBatteryThreshold(lowThreshold: Int) {
        viewModelScope.launch {
            val selectedWatch = selectedWatchManager.selectedWatch.first()
            updateIntSetting(selectedWatch!!.uid, BATTERY_LOW_THRESHOLD_KEY, lowThreshold)
        }
    }

    private suspend fun updateIntSetting(
        watchUid: String,
        key: String,
        value: Int
    ) {
        settingsRepository.putInt(watchUid, key, value)
        intMessageHandler.sendMessage(
            watchUid,
            Message(
                UpdateIntSetting,
                IntSetting(key, value)
            )
        )
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
