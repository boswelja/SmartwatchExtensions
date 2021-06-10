package com.boswelja.smartwatchextensions.batterysync.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.batterysync.BatterySyncWorker
import com.boswelja.smartwatchextensions.batterysync.Utils.updateBatteryStats
import com.boswelja.smartwatchextensions.batterysync.database.WatchBatteryStatsDatabase
import com.boswelja.smartwatchextensions.common.connection.Capability
import com.boswelja.smartwatchextensions.common.preference.PreferenceKey.BATTERY_CHARGE_THRESHOLD_KEY
import com.boswelja.smartwatchextensions.common.preference.PreferenceKey.BATTERY_LOW_THRESHOLD_KEY
import com.boswelja.smartwatchextensions.common.preference.PreferenceKey.BATTERY_PHONE_CHARGE_NOTI_KEY
import com.boswelja.smartwatchextensions.common.preference.PreferenceKey.BATTERY_PHONE_LOW_NOTI_KEY
import com.boswelja.smartwatchextensions.common.preference.PreferenceKey.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.smartwatchextensions.common.preference.PreferenceKey.BATTERY_SYNC_INTERVAL_KEY
import com.boswelja.smartwatchextensions.common.preference.PreferenceKey.BATTERY_WATCH_CHARGE_NOTI_KEY
import com.boswelja.smartwatchextensions.common.preference.PreferenceKey.BATTERY_WATCH_LOW_NOTI_KEY
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import timber.log.Timber

@ExperimentalCoroutinesApi
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

    private val database: WatchBatteryStatsDatabase =
        WatchBatteryStatsDatabase.getInstance(application)

    val batterySyncEnabled = watchManager.getBoolSetting(BATTERY_SYNC_ENABLED_KEY)
    val syncInterval = watchManager.getIntSetting(BATTERY_SYNC_INTERVAL_KEY)
    val phoneChargeNotiEnabled = watchManager.getBoolSetting(BATTERY_PHONE_CHARGE_NOTI_KEY)
    val watchChargeNotiEnabled = watchManager.getBoolSetting(BATTERY_WATCH_CHARGE_NOTI_KEY)
    val chargeThreshold = watchManager.getIntSetting(BATTERY_CHARGE_THRESHOLD_KEY)
    val phoneLowNotiEnabled = watchManager.getBoolSetting(BATTERY_PHONE_LOW_NOTI_KEY)
    val watchLowNotiEnabled = watchManager.getBoolSetting(BATTERY_WATCH_LOW_NOTI_KEY)
    val batteryLowThreshold = watchManager.getIntSetting(BATTERY_LOW_THRESHOLD_KEY)

    val canSyncBattery = watchManager.selectedWatchCapabilities().mapLatest { capabilities ->
        capabilities.contains(Capability.SYNC_BATTERY)
    }

    val batteryStats = watchManager.selectedWatch.flatMapLatest {
        it?.let { database.batteryStatsDao().getStats(it.id) } ?: flow { }
    }

    fun setBatterySyncEnabled(isEnabled: Boolean) {
        viewModelScope.launch(dispatcher) {
            val selectedWatch = watchManager.selectedWatch.first()
            if (isEnabled) {
                val workerStartSuccessful = BatterySyncWorker.startWorker(
                    getApplication(), selectedWatch!!.id
                )
                if (workerStartSuccessful) {
                    watchManager.updatePreference(
                        selectedWatch,
                        BATTERY_SYNC_ENABLED_KEY, isEnabled
                    )
                    updateBatteryStats(getApplication(), selectedWatch)
                } else {
                    Timber.w("Failed to enable battery sync")
                }
            } else {
                watchManager.updatePreference(
                    selectedWatch!!, BATTERY_SYNC_ENABLED_KEY, isEnabled
                )
                BatterySyncWorker.stopWorker(
                    getApplication(), selectedWatch.id
                )
            }
        }
    }

    fun setSyncInterval(syncInterval: Int) {
        viewModelScope.launch(dispatcher) {
            val selectedWatch = watchManager.selectedWatch.first()
            watchManager.updatePreference(
                selectedWatch!!, BATTERY_SYNC_INTERVAL_KEY, syncInterval
            )
            BatterySyncWorker
                .stopWorker(getApplication(), selectedWatch.id)
            BatterySyncWorker
                .startWorker(getApplication(), selectedWatch.id)
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
