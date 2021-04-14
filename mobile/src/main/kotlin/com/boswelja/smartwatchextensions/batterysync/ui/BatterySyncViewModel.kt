package com.boswelja.smartwatchextensions.batterysync.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.batterysync.BatterySyncWorker
import com.boswelja.smartwatchextensions.batterysync.Utils.updateBatteryStats
import com.boswelja.smartwatchextensions.batterysync.database.WatchBatteryStatsDatabase
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
import kotlinx.coroutines.launch
import timber.log.Timber

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

    val batterySyncEnabled = preferenceForSelectedWatch<Boolean>(BATTERY_SYNC_ENABLED_KEY)
    val syncInterval = preferenceForSelectedWatch<Int>(BATTERY_SYNC_INTERVAL_KEY)
    val phoneChargeNotiEnabled = preferenceForSelectedWatch<Boolean>(BATTERY_PHONE_CHARGE_NOTI_KEY)
    val watchChargeNotiEnabled = preferenceForSelectedWatch<Boolean>(BATTERY_WATCH_CHARGE_NOTI_KEY)
    val chargeThreshold = preferenceForSelectedWatch<Int>(BATTERY_CHARGE_THRESHOLD_KEY)
    val phoneLowNotiEnabled = preferenceForSelectedWatch<Boolean>(BATTERY_PHONE_CHARGE_NOTI_KEY)
    val watchLowNotiEnabled = preferenceForSelectedWatch<Boolean>(BATTERY_WATCH_CHARGE_NOTI_KEY)
    val batteryLowThreshold = preferenceForSelectedWatch<Int>(BATTERY_CHARGE_THRESHOLD_KEY)

    val batteryStats = watchManager.selectedWatch.switchMap {
        it?.let {
            database.batteryStatsDao().getObservableStatsForWatch(it.id)
        } ?: liveData { }
    }

    fun setBatterySyncEnabled(isEnabled: Boolean) {
        viewModelScope.launch(dispatcher) {
            if (isEnabled) {
                val workerStartSuccessful = BatterySyncWorker.startWorker(
                    getApplication(), watchManager.selectedWatch.value!!.id
                )
                if (workerStartSuccessful) {
                    watchManager.updatePreference(
                        watchManager.selectedWatch.value!!, BATTERY_SYNC_ENABLED_KEY, isEnabled
                    )
                    updateBatteryStats(getApplication(), watchManager.selectedWatch.value)
                } else {
                    Timber.w("Failed to enable battery sync")
                }
            } else {
                watchManager.updatePreference(
                    watchManager.selectedWatch.value!!, BATTERY_SYNC_ENABLED_KEY, isEnabled
                )
                BatterySyncWorker.stopWorker(
                    getApplication(), watchManager.selectedWatch.value!!.id
                )
            }
        }
    }

    fun setSyncInterval(syncInterval: Int) {
        viewModelScope.launch(dispatcher) {
            watchManager.updatePreference(
                watchManager.selectedWatch.value!!, BATTERY_SYNC_INTERVAL_KEY, syncInterval
            )
            BatterySyncWorker.stopWorker(getApplication(), watchManager.selectedWatch.value!!.id)
            BatterySyncWorker.startWorker(getApplication(), watchManager.selectedWatch.value!!.id)
        }
    }

    fun setChargeThreshold(chargeThreshold: Int) {
        viewModelScope.launch(dispatcher) {
            watchManager.updatePreference(
                watchManager.selectedWatch.value!!, BATTERY_CHARGE_THRESHOLD_KEY, chargeThreshold
            )
        }
    }

    fun setPhoneChargeNotiEnabled(isEnabled: Boolean) {
        viewModelScope.launch(dispatcher) {
            watchManager.updatePreference(
                watchManager.selectedWatch.value!!, BATTERY_PHONE_CHARGE_NOTI_KEY, isEnabled
            )
        }
    }

    fun setWatchChargeNotiEnabled(isEnabled: Boolean) {
        viewModelScope.launch(dispatcher) {
            watchManager.updatePreference(
                watchManager.selectedWatch.value!!, BATTERY_WATCH_CHARGE_NOTI_KEY, isEnabled
            )
        }
    }

    fun setPhoneLowNotiEnabled(isEnabled: Boolean) {
        viewModelScope.launch(dispatcher) {
            watchManager.updatePreference(
                watchManager.selectedWatch.value!!, BATTERY_PHONE_LOW_NOTI_KEY, isEnabled
            )
        }
    }

    fun setWatchLowNotiEnabled(isEnabled: Boolean) {
        viewModelScope.launch(dispatcher) {
            watchManager.updatePreference(
                watchManager.selectedWatch.value!!, BATTERY_WATCH_LOW_NOTI_KEY, isEnabled
            )
        }
    }

    fun setLowBatteryThreshold(lowThreshold: Int) {
        viewModelScope.launch(dispatcher) {
            watchManager.updatePreference(
                watchManager.selectedWatch.value!!, BATTERY_LOW_THRESHOLD_KEY, lowThreshold
            )
        }
    }

    private inline fun <reified T> preferenceForSelectedWatch(key: String): LiveData<T?> {
        return watchManager.selectedWatch.switchMap {
            it?.let {
                watchManager.getPreferenceObservable<T>(it.id, key)
            } ?: liveData { }
        }
    }
}
