package com.boswelja.devicemanager.batterysync.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.boswelja.devicemanager.batterysync.BatterySyncWorker
import com.boswelja.devicemanager.batterysync.Utils.updateBatteryStats
import com.boswelja.devicemanager.batterysync.database.WatchBatteryStatsDatabase
import com.boswelja.devicemanager.common.preference.PreferenceKey.BATTERY_CHARGE_THRESHOLD_KEY
import com.boswelja.devicemanager.common.preference.PreferenceKey.BATTERY_PHONE_CHARGE_NOTI_KEY
import com.boswelja.devicemanager.common.preference.PreferenceKey.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.devicemanager.common.preference.PreferenceKey.BATTERY_SYNC_INTERVAL_KEY
import com.boswelja.devicemanager.common.preference.PreferenceKey.BATTERY_WATCH_CHARGE_NOTI_KEY
import com.boswelja.devicemanager.watchmanager.WatchManager
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
        Dispatchers.getIO()
    )

    private val database: WatchBatteryStatsDatabase =
        WatchBatteryStatsDatabase.getInstance(application)

    val batterySyncEnabled = watchManager.selectedWatch.switchMap {
        it?.let {
            watchManager.getPreferenceObservable<Boolean>(it.id, BATTERY_SYNC_ENABLED_KEY)
        } ?: liveData { }
    }
    val phoneChargeNotiEnabled = watchManager.selectedWatch.switchMap {
        it?.let {
            watchManager.getPreferenceObservable<Boolean>(it.id, BATTERY_PHONE_CHARGE_NOTI_KEY)
        } ?: liveData { }
    }
    val watchChargeNotiEnabled = watchManager.selectedWatch.switchMap {
        it?.let {
            watchManager.getPreferenceObservable<Boolean>(it.id, BATTERY_WATCH_CHARGE_NOTI_KEY)
        } ?: liveData { }
    }
    val chargeThreshold = watchManager.selectedWatch.switchMap {
        it?.let {
            watchManager.getPreferenceObservable<Int>(it.id, BATTERY_CHARGE_THRESHOLD_KEY)
        } ?: liveData { }
    }
    val syncInterval = watchManager.selectedWatch.switchMap {
        it?.let {
            watchManager.getPreferenceObservable<Int>(it.id, BATTERY_SYNC_INTERVAL_KEY)
        } ?: liveData { }
    }

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
}
