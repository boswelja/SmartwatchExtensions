package com.boswelja.smartwatchextensions.batterysync.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.batterysync.BaseBatterySyncWorker
import com.boswelja.smartwatchextensions.batterysync.BatteryStatsRepository
import com.boswelja.smartwatchextensions.batterysync.BatteryStatsRepositoryLoader
import com.boswelja.smartwatchextensions.batterysync.BatterySyncWorker
import com.boswelja.smartwatchextensions.batterysync.quicksettings.WatchBatteryTileService
import com.boswelja.smartwatchextensions.common.WatchWidgetProvider
import com.boswelja.smartwatchextensions.devicemanagement.Capability
import com.boswelja.smartwatchextensions.settings.BoolSettingKeys.BATTERY_PHONE_CHARGE_NOTI_KEY
import com.boswelja.smartwatchextensions.settings.BoolSettingKeys.BATTERY_PHONE_LOW_NOTI_KEY
import com.boswelja.smartwatchextensions.settings.BoolSettingKeys.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.smartwatchextensions.settings.BoolSettingKeys.BATTERY_WATCH_CHARGE_NOTI_KEY
import com.boswelja.smartwatchextensions.settings.BoolSettingKeys.BATTERY_WATCH_LOW_NOTI_KEY
import com.boswelja.smartwatchextensions.settings.IntSettingKeys.BATTERY_CHARGE_THRESHOLD_KEY
import com.boswelja.smartwatchextensions.settings.IntSettingKeys.BATTERY_LOW_THRESHOLD_KEY
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalCoroutinesApi::class)
class BatterySyncViewModel internal constructor(
    application: Application,
    private val watchManager: WatchManager,
    private val dispatcher: CoroutineDispatcher
) : AndroidViewModel(application) {

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        WatchManager.getInstance(application),
        Dispatchers.IO
    )

    private val repository: BatteryStatsRepository =
        BatteryStatsRepositoryLoader.getInstance(application)

    val batterySyncEnabled = watchManager.getBoolSetting(BATTERY_SYNC_ENABLED_KEY)
    val phoneChargeNotiEnabled = watchManager.getBoolSetting(BATTERY_PHONE_CHARGE_NOTI_KEY)
    val watchChargeNotiEnabled = watchManager.getBoolSetting(BATTERY_WATCH_CHARGE_NOTI_KEY)
    val chargeThreshold = watchManager.getIntSetting(BATTERY_CHARGE_THRESHOLD_KEY)
    val phoneLowNotiEnabled = watchManager.getBoolSetting(BATTERY_PHONE_LOW_NOTI_KEY)
    val watchLowNotiEnabled = watchManager.getBoolSetting(BATTERY_WATCH_LOW_NOTI_KEY)
    val batteryLowThreshold = watchManager.getIntSetting(BATTERY_LOW_THRESHOLD_KEY)

    val canSyncBattery = watchManager.selectedWatchHasCapability(Capability.SYNC_BATTERY)

    val batteryStats = watchManager.selectedWatch.flatMapLatest {
        it?.let { repository.batteryStatsFor(it.uid) } ?: flow { }
    }

    fun setBatterySyncEnabled(isEnabled: Boolean) {
        viewModelScope.launch(dispatcher) {
            val selectedWatch = watchManager.selectedWatch.first()
            if (isEnabled) {
                val workerStartSuccessful = BaseBatterySyncWorker
                    .startSyncingFor<BatterySyncWorker>(getApplication(), selectedWatch!!.uid)
                if (workerStartSuccessful) {
                    watchManager.updatePreference(
                        selectedWatch,
                        BATTERY_SYNC_ENABLED_KEY, isEnabled
                    )
                } else {
                    Timber.w("Failed to enable battery sync")
                }
            } else {
                watchManager.updatePreference(
                    selectedWatch!!, BATTERY_SYNC_ENABLED_KEY, isEnabled
                )
                BaseBatterySyncWorker.stopSyncingFor(
                    getApplication(), selectedWatch.uid
                )
                WatchBatteryTileService.requestTileUpdate(getApplication())
                WatchWidgetProvider.updateWidgets(getApplication())
            }
        }
    }

    fun setChargeThreshold(chargeThreshold: Int) {
        viewModelScope.launch(dispatcher) {
            val selectedWatch = watchManager.selectedWatch.first()
            watchManager.updatePreference(
                selectedWatch!!,
                BATTERY_CHARGE_THRESHOLD_KEY, chargeThreshold
            )
        }
    }

    fun setPhoneChargeNotiEnabled(isEnabled: Boolean) {
        viewModelScope.launch(dispatcher) {
            val selectedWatch = watchManager.selectedWatch.first()
            watchManager.updatePreference(
                selectedWatch!!, BATTERY_PHONE_CHARGE_NOTI_KEY, isEnabled
            )
        }
    }

    fun setWatchChargeNotiEnabled(isEnabled: Boolean) {
        viewModelScope.launch(dispatcher) {
            val selectedWatch = watchManager.selectedWatch.first()
            watchManager.updatePreference(
                selectedWatch!!, BATTERY_WATCH_CHARGE_NOTI_KEY, isEnabled
            )
        }
    }

    fun setPhoneLowNotiEnabled(isEnabled: Boolean) {
        viewModelScope.launch(dispatcher) {
            val selectedWatch = watchManager.selectedWatch.first()
            watchManager.updatePreference(
                selectedWatch!!, BATTERY_PHONE_LOW_NOTI_KEY, isEnabled
            )
        }
    }

    fun setWatchLowNotiEnabled(isEnabled: Boolean) {
        viewModelScope.launch(dispatcher) {
            val selectedWatch = watchManager.selectedWatch.first()
            watchManager.updatePreference(
                selectedWatch!!, BATTERY_WATCH_LOW_NOTI_KEY, isEnabled
            )
        }
    }

    fun setLowBatteryThreshold(lowThreshold: Int) {
        viewModelScope.launch(dispatcher) {
            val selectedWatch = watchManager.selectedWatch.first()
            watchManager.updatePreference(
                selectedWatch!!, BATTERY_LOW_THRESHOLD_KEY, lowThreshold
            )
        }
    }
}
