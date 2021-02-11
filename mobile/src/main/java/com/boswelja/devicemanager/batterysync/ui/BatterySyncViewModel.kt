/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.batterysync.ui

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.batterysync.database.WatchBatteryStats
import com.boswelja.devicemanager.batterysync.database.WatchBatteryStatsDatabase
import com.boswelja.devicemanager.common.preference.PreferenceKey.BATTERY_SYNC_ENABLED_KEY

class BatterySyncViewModel(application: Application) : AndroidViewModel(application) {

    private val database: WatchBatteryStatsDatabase =
        WatchBatteryStatsDatabase.getInstance(application)

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)
    private val preferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            when (key) {
                BATTERY_SYNC_ENABLED_KEY -> {
                    _batterySyncEnabled.postValue(sharedPreferences.getBoolean(key, false))
                }
            }
        }

    private val _batterySyncEnabled =
        MutableLiveData(sharedPreferences.getBoolean(BATTERY_SYNC_ENABLED_KEY, false))
    val batterySyncEnabled: LiveData<Boolean>
        get() = _batterySyncEnabled

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    override fun onCleared() {
        super.onCleared()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    fun getBatteryStatsObservable(watchId: String): LiveData<WatchBatteryStats?> {
        return database.batteryStatsDao().getObservableStatsForWatch(watchId)
    }
}
