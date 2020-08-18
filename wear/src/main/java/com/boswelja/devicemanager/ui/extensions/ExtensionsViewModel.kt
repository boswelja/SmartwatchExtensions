/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.extensions

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_PERCENT_KEY
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.devicemanager.common.PreferenceKey.PHONE_LOCKING_ENABLED_KEY

class ExtensionsViewModel(application: Application) : AndroidViewModel(application) {

  private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)
  private val preferenceChangeListener =
      SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
          PHONE_LOCKING_ENABLED_KEY ->
              _phoneLockingEnabled.postValue(sharedPreferences.getBoolean(key, false))
          BATTERY_SYNC_ENABLED_KEY ->
              _batterySyncEnabled.postValue(sharedPreferences.getBoolean(key, false))
          BATTERY_PERCENT_KEY -> _phoneBatteryPercent.postValue(sharedPreferences.getInt(key, 0))
        }
      }

  private val _phoneConnected = MutableLiveData(false)
  val phoneConnected: LiveData<Boolean>
    get() = _phoneConnected

  private val _phoneLockingEnabled =
      MutableLiveData(sharedPreferences.getBoolean(PHONE_LOCKING_ENABLED_KEY, false))
  val phoneLockingEnabled: LiveData<Boolean>
    get() = _phoneLockingEnabled

  private val _batterySyncEnabled =
      MutableLiveData(sharedPreferences.getBoolean(BATTERY_SYNC_ENABLED_KEY, false))
  val batterySyncEnabled: LiveData<Boolean>
    get() = _batterySyncEnabled

  private val _phoneBatteryPercent =
      MutableLiveData(sharedPreferences.getInt(BATTERY_PERCENT_KEY, 0))
  val phoneBatteryPercent: LiveData<Int>
    get() = _phoneBatteryPercent

  init {
    sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
  }

  override fun onCleared() {
    super.onCleared()
    sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
  }
}
