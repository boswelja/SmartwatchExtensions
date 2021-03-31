package com.boswelja.devicemanager.batterysync.ui

import android.app.Application
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
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
    private val sharedPreferences: SharedPreferences,
    private val dispatcher: CoroutineDispatcher
) : AndroidViewModel(application) {

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        WatchManager.getInstance(application),
        PreferenceManager.getDefaultSharedPreferences(application),
        Dispatchers.IO
    )

    private val database: WatchBatteryStatsDatabase =
        WatchBatteryStatsDatabase.getInstance(application)

    private val _batterySyncEnabled =
        MutableLiveData(sharedPreferences.getBoolean(BATTERY_SYNC_ENABLED_KEY, false))
    private val _phoneChargeNotiEnabled =
        MutableLiveData(sharedPreferences.getBoolean(BATTERY_PHONE_CHARGE_NOTI_KEY, false))
    private val _watchChargeNotiEnabled =
        MutableLiveData(sharedPreferences.getBoolean(BATTERY_WATCH_CHARGE_NOTI_KEY, false))
    private val _chargeThreshold =
        MutableLiveData(sharedPreferences.getInt(BATTERY_CHARGE_THRESHOLD_KEY, 90))
    private val _syncInterval =
        MutableLiveData(sharedPreferences.getInt(BATTERY_SYNC_INTERVAL_KEY, 15))

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

    fun setBatterySyncEnabled(isEnabled: Boolean) {
        viewModelScope.launch(dispatcher) {
            if (isEnabled) {
                val workerStartSuccessful = BatterySyncWorker.startWorker(
                    getApplication(), watchManager.selectedWatch.value!!.id
                )
                if (workerStartSuccessful) {
                    _batterySyncEnabled.postValue(isEnabled)
                    sharedPreferences.edit(commit = true) {
                        putBoolean(BATTERY_SYNC_ENABLED_KEY, isEnabled)
                    }
                    watchManager.updatePreference(
                        watchManager.selectedWatch.value!!, BATTERY_SYNC_ENABLED_KEY, isEnabled
                    )
                    updateBatteryStats(getApplication(), watchManager.selectedWatch.value)
                } else {
                    Timber.w("Failed to enable battery sync")
                }
            } else {
                _batterySyncEnabled.postValue(isEnabled)
                sharedPreferences.edit(commit = true) {
                    putBoolean(BATTERY_SYNC_ENABLED_KEY, isEnabled)
                }
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
            sharedPreferences.edit(commit = true) {
                putInt(BATTERY_SYNC_INTERVAL_KEY, syncInterval)
            }
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
            sharedPreferences.edit(commit = true) {
                putInt(BATTERY_CHARGE_THRESHOLD_KEY, chargeThreshold)
            }
            watchManager.updatePreference(
                watchManager.selectedWatch.value!!, BATTERY_CHARGE_THRESHOLD_KEY, chargeThreshold
            )
        }
    }

    fun setPhoneChargeNotiEnabled(isEnabled: Boolean) {
        viewModelScope.launch(dispatcher) {
            sharedPreferences.edit(commit = true) {
                putBoolean(BATTERY_PHONE_CHARGE_NOTI_KEY, isEnabled)
            }
            watchManager.updatePreference(
                watchManager.selectedWatch.value!!, BATTERY_PHONE_CHARGE_NOTI_KEY, chargeThreshold
            )
            _phoneChargeNotiEnabled.postValue(isEnabled)
        }
    }

    fun setWatchChargeNotiEnabled(isEnabled: Boolean) {
        viewModelScope.launch(dispatcher) {
            sharedPreferences.edit(commit = true) {
                putBoolean(BATTERY_WATCH_CHARGE_NOTI_KEY, isEnabled)
            }
            watchManager.updatePreference(
                watchManager.selectedWatch.value!!, BATTERY_WATCH_CHARGE_NOTI_KEY, chargeThreshold
            )
            _watchChargeNotiEnabled.postValue(isEnabled)
        }
    }
}
