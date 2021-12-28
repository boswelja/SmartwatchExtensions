package com.boswelja.smartwatchextensions.batterysync.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.batterysync.BatteryStatsRepository
import com.boswelja.smartwatchextensions.batterysync.BatterySyncWorker
import com.boswelja.smartwatchextensions.batterysync.quicksettings.WatchBatteryTileService
import com.boswelja.smartwatchextensions.common.WatchWidgetProvider
import com.boswelja.smartwatchextensions.devicemanagement.Capability
import com.boswelja.smartwatchextensions.devicemanagement.WatchManager
import com.boswelja.smartwatchextensions.settings.BoolSettingKeys.BATTERY_PHONE_CHARGE_NOTI_KEY
import com.boswelja.smartwatchextensions.settings.BoolSettingKeys.BATTERY_PHONE_LOW_NOTI_KEY
import com.boswelja.smartwatchextensions.settings.BoolSettingKeys.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.smartwatchextensions.settings.BoolSettingKeys.BATTERY_WATCH_CHARGE_NOTI_KEY
import com.boswelja.smartwatchextensions.settings.BoolSettingKeys.BATTERY_WATCH_LOW_NOTI_KEY
import com.boswelja.smartwatchextensions.settings.IntSettingKeys.BATTERY_CHARGE_THRESHOLD_KEY
import com.boswelja.smartwatchextensions.settings.IntSettingKeys.BATTERY_LOW_THRESHOLD_KEY
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

/**
 * A ViewModel to provide data for Battery Sync
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BatterySyncViewModel(
    application: Application,
    private val watchManager: WatchManager,
    private val repository: BatteryStatsRepository
) : AndroidViewModel(application) {

    /**
     * Flow whether Battery Sync is enabled for the selected watch.
     */
    val batterySyncEnabled = watchManager.getBoolSetting(BATTERY_SYNC_ENABLED_KEY)

    /**
     * Flow whether phone charge notifications are enabled for the selected watch.
     */
    val phoneChargeNotiEnabled = watchManager.getBoolSetting(BATTERY_PHONE_CHARGE_NOTI_KEY)

    /**
     * Flow whether watch charge notifications are enabled for the selected watch.
     */
    val watchChargeNotiEnabled = watchManager.getBoolSetting(BATTERY_WATCH_CHARGE_NOTI_KEY)

    /**
     * Flow the charge percent threshold.
     */
    val chargeThreshold = watchManager.getIntSetting(BATTERY_CHARGE_THRESHOLD_KEY)

    /**
     * Flow whether phone low notifications are enabled for the selected watch.
     */
    val phoneLowNotiEnabled = watchManager.getBoolSetting(BATTERY_PHONE_LOW_NOTI_KEY)

    /**
     * Flow whether watch low notifications are enabled for the selected watch.
     */
    val watchLowNotiEnabled = watchManager.getBoolSetting(BATTERY_WATCH_LOW_NOTI_KEY)

    /**
     * Flow the low percent threshold.
     */
    val batteryLowThreshold = watchManager.getIntSetting(BATTERY_LOW_THRESHOLD_KEY)

    /**
     * Flow whether the selected watch supports Battery Sync.
     */
    val canSyncBattery = watchManager.selectedWatchHasCapability(Capability.SYNC_BATTERY)

    /**
     * Flow the stored battery stats for the selected watch.
     */
    val batteryStats = watchManager.selectedWatch.flatMapLatest {
        it?.let { repository.batteryStatsFor(it.uid) } ?: flow { }
    }

    /**
     * Set whether Battery Sync is enabled.
     */
    fun setBatterySyncEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            val selectedWatch = watchManager.selectedWatch.first()
            if (isEnabled) {
                val workerStartSuccessful = BatterySyncWorker
                    .startSyncingFor(getApplication(), selectedWatch!!.uid)
                if (workerStartSuccessful) {
                    watchManager.updatePreference(
                        selectedWatch,
                        BATTERY_SYNC_ENABLED_KEY, isEnabled
                    )
                }
            } else {
                watchManager.updatePreference(
                    selectedWatch!!, BATTERY_SYNC_ENABLED_KEY, isEnabled
                )
                BatterySyncWorker.stopSyncingFor(
                    getApplication(), selectedWatch.uid
                )
                WatchBatteryTileService.requestTileUpdate(getApplication())
                WatchWidgetProvider.updateWidgets(getApplication())
            }
        }
    }

    /**
     * Set the charge notification threshold.
     */
    fun setChargeThreshold(chargeThreshold: Int) {
        viewModelScope.launch {
            val selectedWatch = watchManager.selectedWatch.first()
            watchManager.updatePreference(
                selectedWatch!!,
                BATTERY_CHARGE_THRESHOLD_KEY, chargeThreshold
            )
        }
    }

    /**
     * Set whether phone charge notifications are enabled.
     */
    fun setPhoneChargeNotiEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            val selectedWatch = watchManager.selectedWatch.first()
            watchManager.updatePreference(
                selectedWatch!!, BATTERY_PHONE_CHARGE_NOTI_KEY, isEnabled
            )
        }
    }

    /**
     * Set whether watch charge notifications are enabled.
     */
    fun setWatchChargeNotiEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            val selectedWatch = watchManager.selectedWatch.first()
            watchManager.updatePreference(
                selectedWatch!!, BATTERY_WATCH_CHARGE_NOTI_KEY, isEnabled
            )
        }
    }

    /**
     * Set whether phone low notifications are enabled.
     */
    fun setPhoneLowNotiEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            val selectedWatch = watchManager.selectedWatch.first()
            watchManager.updatePreference(
                selectedWatch!!, BATTERY_PHONE_LOW_NOTI_KEY, isEnabled
            )
        }
    }

    /**
     * Set whether watch low notifications are enabled.
     */
    fun setWatchLowNotiEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            val selectedWatch = watchManager.selectedWatch.first()
            watchManager.updatePreference(
                selectedWatch!!, BATTERY_WATCH_LOW_NOTI_KEY, isEnabled
            )
        }
    }

    /**
     * Set the low battery notification threshold.
     */
    fun setLowBatteryThreshold(lowThreshold: Int) {
        viewModelScope.launch {
            val selectedWatch = watchManager.selectedWatch.first()
            watchManager.updatePreference(
                selectedWatch!!, BATTERY_LOW_THRESHOLD_KEY, lowThreshold
            )
        }
    }
}
