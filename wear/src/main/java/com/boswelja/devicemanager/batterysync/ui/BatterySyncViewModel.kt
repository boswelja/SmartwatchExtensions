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
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.ConfirmationActivityHandler
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_PERCENT_KEY
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.devicemanager.common.batterysync.References.REQUEST_BATTERY_UPDATE_PATH
import com.boswelja.devicemanager.phoneconnectionmanager.References
import com.boswelja.devicemanager.phoneconnectionmanager.References.PHONE_CONNECTED_KEY
import com.boswelja.devicemanager.phoneconnectionmanager.References.PHONE_NAME_KEY
import com.google.android.gms.wearable.Wearable

class BatterySyncViewModel(application: Application) : AndroidViewModel(application) {

  private val batterySyncDisabledString by lazy {
    application.getString(R.string.battery_sync_disabled)
  }

  private val messageClient = Wearable.getMessageClient(application)
  private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)
  private val phoneId by lazy { sharedPreferences.getString(References.PHONE_ID_KEY, "") ?: "" }
  private val preferenceChangeListener =
      SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        when (key) {
          BATTERY_SYNC_ENABLED_KEY ->
              _batterySyncEnabled.postValue(sharedPreferences.getBoolean(key, false))
          BATTERY_PERCENT_KEY -> _batteryPercent.postValue(sharedPreferences.getInt(key, 0))
          PHONE_NAME_KEY ->
              _phoneName.postValue(
                  sharedPreferences.getString(
                      key, application.getString(R.string.default_phone_name)))
          PHONE_CONNECTED_KEY -> _phoneConnected.postValue(sharedPreferences.getBoolean(key, false))
        }
      }

  private val _phoneName =
      MutableLiveData(
          sharedPreferences.getString(
              PHONE_NAME_KEY, application.getString(R.string.default_phone_name))
              ?: application.getString(R.string.default_phone_name))
  val phoneName: LiveData<String>
    get() = _phoneName

  private val _displayText = MediatorLiveData<String>()
  val displayText: LiveData<String>
    get() = _displayText

  private val _phoneConnected = MutableLiveData(false)
  val phoneConnected: LiveData<Boolean>
    get() = _phoneConnected

  private val _batterySyncEnabled =
      MutableLiveData(sharedPreferences.getBoolean(BATTERY_SYNC_ENABLED_KEY, false))
  val batterySyncEnabled: LiveData<Boolean>
    get() = _batterySyncEnabled

  private val _batteryPercent = MutableLiveData(sharedPreferences.getInt(BATTERY_PERCENT_KEY, 0))
  val batteryPercent: LiveData<Int>
    get() = _batteryPercent

  init {
    sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    _displayText.addSource(batterySyncEnabled) {
      if (it)
          _displayText.postValue(
              application.getString(
                  R.string.battery_percent, batteryPercent.value.toString()))
      else _displayText.postValue(batterySyncDisabledString)
    }
    _displayText.addSource(batteryPercent) {
      if (batterySyncEnabled.value == true)
          _displayText.postValue(
              application.getString(R.string.battery_percent, it.toString()))
      else _displayText.postValue(batterySyncDisabledString)
    }
  }

  override fun onCleared() {
    super.onCleared()
    sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
  }

  fun updateBatteryStats() {
    val isBatterySyncEnabled = batterySyncEnabled.value == true
    val isPhoneConnected = phoneConnected.value == true
    if (isPhoneConnected && isBatterySyncEnabled) {
      ConfirmationActivityHandler.successAnimation(getApplication())
      messageClient.sendMessage(phoneId, REQUEST_BATTERY_UPDATE_PATH, null)
    } else if (!isBatterySyncEnabled) {
      ConfirmationActivityHandler.failAnimation(getApplication(), batterySyncDisabledString)
    } else {
      ConfirmationActivityHandler.failAnimation(getApplication())
    }
  }
}
