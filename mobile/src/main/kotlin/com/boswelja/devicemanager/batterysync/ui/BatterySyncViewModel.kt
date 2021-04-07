package com.boswelja.devicemanager.batterysync.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
        Dispatchers.IO
    )

    private val database: WatchBatteryStatsDatabase =
        WatchBatteryStatsDatabase.getInstance(application)

    private val _batterySyncEnabled = MutableLiveData<Boolean>()
    private val _phoneChargeNotiEnabled = MutableLiveData<Boolean>()
    private val _watchChargeNotiEnabled = MutableLiveData<Boolean>()
    private val _chargeThreshold = MutableLiveData<Int>()
    private val _syncInterval = MutableLiveData<Int>()

    val batterySyncEnabled: LiveData<Boolean>
        get() = _batterySyncEnabled
    val phoneChargeNotiEnabled: LiveData<Boolean>
        get() = _phoneChargeNotiEnabled
    val watchChargeNotiEnabled: LiveData<Boolean>
        get() = _watchChargeNotiEnabled
    val chargeThreshold: LiveData<Int>
        get() = _chargeThreshold
    val syncInterval: LiveData<Int>
        get() = _syncInterval

    val batteryStats = watchManager.selectedWatch.switchMap {
        if (it != null) database.batteryStatsDao().getObservableStatsForWatch(it.id)
        else liveData { }
    }

    init {
        watchManager.selectedWatch.value?.let { selectedWatch ->
            viewModelScope.launch {
                _batterySyncEnabled.postValue(
                    watchManager.getPreference(selectedWatch, BATTERY_SYNC_ENABLED_KEY)
                )
                _phoneChargeNotiEnabled.postValue(
                    watchManager.getPreference(selectedWatch, BATTERY_PHONE_CHARGE_NOTI_KEY)
                )
                _watchChargeNotiEnabled.postValue(
                    watchManager.getPreference(selectedWatch, BATTERY_WATCH_CHARGE_NOTI_KEY)
                )
                _chargeThreshold.postValue(
                    watchManager.getPreference(selectedWatch, BATTERY_CHARGE_THRESHOLD_KEY)
                )
                _syncInterval.postValue(
                    watchManager.getPreference(selectedWatch, BATTERY_SYNC_INTERVAL_KEY)
                )
            }
        }
    }

    fun setBatterySyncEnabled(isEnabled: Boolean) {
        viewModelScope.launch(dispatcher) {
            if (isEnabled) {
                val workerStartSuccessful = BatterySyncWorker.startWorker(
                    getApplication(), watchManager.selectedWatch.value!!.id
                )
                if (workerStartSuccessful) {
                    _batterySyncEnabled.postValue(isEnabled)
                    watchManager.updatePreference(
                        watchManager.selectedWatch.value!!, BATTERY_SYNC_ENABLED_KEY, isEnabled
                    )
                    updateBatteryStats(getApplication(), watchManager.selectedWatch.value)
                } else {
                    Timber.w("Failed to enable battery sync")
                }
            } else {
                _batterySyncEnabled.postValue(isEnabled)
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
        _syncInterval.postValue(syncInterval)
        viewModelScope.launch(dispatcher) {
            watchManager.updatePreference(
                watchManager.selectedWatch.value!!, BATTERY_SYNC_INTERVAL_KEY, syncInterval
            )
            BatterySyncWorker.stopWorker(getApplication(), watchManager.selectedWatch.value!!.id)
            BatterySyncWorker.startWorker(getApplication(), watchManager.selectedWatch.value!!.id)
        }
    }

    fun setChargeThreshold(chargeThreshold: Int) {
        _chargeThreshold.postValue(chargeThreshold)
        viewModelScope.launch(dispatcher) {
            watchManager.updatePreference(
                watchManager.selectedWatch.value!!, BATTERY_CHARGE_THRESHOLD_KEY, chargeThreshold
            )
        }
    }

    fun setPhoneChargeNotiEnabled(isEnabled: Boolean) {
        viewModelScope.launch(dispatcher) {
            watchManager.updatePreference(
                watchManager.selectedWatch.value!!, BATTERY_PHONE_CHARGE_NOTI_KEY, chargeThreshold
            )
            _phoneChargeNotiEnabled.postValue(isEnabled)
        }
    }

    fun setWatchChargeNotiEnabled(isEnabled: Boolean) {
        viewModelScope.launch(dispatcher) {
            watchManager.updatePreference(
                watchManager.selectedWatch.value!!, BATTERY_WATCH_CHARGE_NOTI_KEY, chargeThreshold
            )
            _watchChargeNotiEnabled.postValue(isEnabled)
        }
    }
}
