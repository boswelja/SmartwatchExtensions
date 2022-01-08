package com.boswelja.smartwatchextensions.batterysync.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.batterysync.BatteryStatsRepository
import com.boswelja.smartwatchextensions.batterysync.BatterySyncWorker
import com.boswelja.smartwatchextensions.batterysync.DefaultValues
import com.boswelja.smartwatchextensions.devicemanagement.Capability
import com.boswelja.smartwatchextensions.devicemanagement.SelectedWatchManager
import com.boswelja.smartwatchextensions.settings.BoolSetting
import com.boswelja.smartwatchextensions.settings.BoolSettingKeys
import com.boswelja.smartwatchextensions.settings.BoolSettingKeys.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.smartwatchextensions.settings.BoolSettingSerializer
import com.boswelja.smartwatchextensions.settings.IntSetting
import com.boswelja.smartwatchextensions.settings.IntSettingKeys.BATTERY_CHARGE_THRESHOLD_KEY
import com.boswelja.smartwatchextensions.settings.IntSettingKeys.BATTERY_LOW_THRESHOLD_KEY
import com.boswelja.smartwatchextensions.settings.IntSettingSerializer
import com.boswelja.smartwatchextensions.settings.UPDATE_BOOL_PREFERENCE
import com.boswelja.smartwatchextensions.settings.UPDATE_INT_PREFERENCE
import com.boswelja.smartwatchextensions.settings.WatchSettingsRepository
import com.boswelja.watchconnection.common.Watch
import com.boswelja.watchconnection.common.message.Message
import com.boswelja.watchconnection.core.discovery.DiscoveryClient
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
 * A ViewModel to provide data for Battery Sync
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BatterySyncViewModel(
    application: Application,
    messageClient: MessageClient,
    discoveryClient: DiscoveryClient,
    private val selectedWatchManager: SelectedWatchManager,
    private val settingsRepository: WatchSettingsRepository,
    private val batteryStatsRepository: BatteryStatsRepository
) : AndroidViewModel(application) {

    private val boolMessageHandler = MessageHandler(BoolSettingSerializer, messageClient)
    private val intMessageHandler = MessageHandler(IntSettingSerializer, messageClient)

    /**
     * Flow whether Battery Sync is enabled for the selected watch.
     */
    val batterySyncEnabled = mapStateForSelectedWatch(false) {
        settingsRepository.getBoolean(it.uid, BATTERY_SYNC_ENABLED_KEY)
    }

    /**
     * Flow the charge percent threshold.
     */
    val chargeThreshold = mapStateForSelectedWatch(DefaultValues.CHARGE_THRESHOLD) {
        settingsRepository.getInt(it.uid, BATTERY_CHARGE_THRESHOLD_KEY)
    }

    /**
     * Flow the low percent threshold.
     */
    val batteryLowThreshold = mapStateForSelectedWatch(DefaultValues.LOW_THRESHOLD) {
        settingsRepository.getInt(it.uid, BATTERY_LOW_THRESHOLD_KEY)
    }

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
     * Flow whether phone charge notifications are enabled for the selected watch.
     */
    val phoneChargeNotiEnabled = mapStateForSelectedWatch(false) {
        settingsRepository.getBoolean(it.uid, BoolSettingKeys.BATTERY_PHONE_CHARGE_NOTI_KEY)
    }

    /**
     * Flow whether phone low notifications are enabled for the selected watch.
     */
    val phoneLowNotiEnabled = mapStateForSelectedWatch(DefaultValues.NOTIFICATIONS_ENABLED) {
        settingsRepository.getBoolean(it.uid, BoolSettingKeys.BATTERY_PHONE_LOW_NOTI_KEY)
    }

    /**
     * Flow whether the selected watch supports Battery Sync.
     */
    val canSyncBattery = mapStateForSelectedWatch(false) {
        discoveryClient.hasCapability(it.uid, Capability.SYNC_BATTERY.name)
    }

    /**
     * Flow the stored battery stats for the selected watch.
     */
    val batteryStats = mapStateForSelectedWatch(null) {
        batteryStatsRepository.batteryStatsFor(it.uid)
    }

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
                UPDATE_INT_PREFERENCE,
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
                UPDATE_BOOL_PREFERENCE,
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
