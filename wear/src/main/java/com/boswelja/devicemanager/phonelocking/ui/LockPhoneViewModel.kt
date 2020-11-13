/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.phonelocking.ui

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.ConfirmationActivityHandler
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.References.LOCK_PHONE_PATH
import com.boswelja.devicemanager.common.preference.PreferenceKey.PHONE_LOCKING_ENABLED_KEY
import com.boswelja.devicemanager.phoneconnectionmanager.References
import com.boswelja.devicemanager.phoneconnectionmanager.References.PHONE_CONNECTED_KEY
import com.boswelja.devicemanager.phoneconnectionmanager.References.PHONE_NAME_KEY
import com.google.android.gms.wearable.Wearable

class LockPhoneViewModel(application: Application) : AndroidViewModel(application) {

    private val phoneLockingDisabledString by lazy {
        application.getString(R.string.lock_phone_disabled)
    }

    private val messageClient = Wearable.getMessageClient(application)
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)
    private val phoneId by lazy { sharedPreferences.getString(References.PHONE_ID_KEY, "") ?: "" }
    private val preferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            when (key) {
                PHONE_LOCKING_ENABLED_KEY ->
                    _phoneLockingEnabled.postValue(sharedPreferences.getBoolean(key, false))
                PHONE_NAME_KEY ->
                    _phoneName.postValue(
                        sharedPreferences.getString(
                            key, application.getString(R.string.default_phone_name)
                        )
                    )
                PHONE_CONNECTED_KEY ->
                    _phoneConnected.postValue(sharedPreferences.getBoolean(key, false))
            }
        }

    private val _phoneName =
        MutableLiveData(
            sharedPreferences.getString(
                PHONE_NAME_KEY, application.getString(R.string.default_phone_name)
            )
                ?: application.getString(R.string.default_phone_name)
        )
    val phoneName: LiveData<String>
        get() = _phoneName

    private val _displayText = MediatorLiveData<String>()
    val displayText: LiveData<String>
        get() = _displayText

    private val _phoneConnected = MutableLiveData(
        sharedPreferences.getBoolean(PHONE_CONNECTED_KEY, false)
    )
    val phoneConnected: LiveData<Boolean>
        get() = _phoneConnected

    private val _phoneLockingEnabled =
        MutableLiveData(sharedPreferences.getBoolean(PHONE_LOCKING_ENABLED_KEY, false))
    val phoneLockingEnabled: LiveData<Boolean>
        get() = _phoneLockingEnabled

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
        _displayText.addSource(phoneLockingEnabled) {
            if (it)
                _displayText.postValue(application.getString(R.string.lock_phone, phoneName.value))
            else _displayText.postValue(phoneLockingDisabledString)
        }
        _displayText.addSource(phoneName) {
            if (phoneLockingEnabled.value == true)
                _displayText.postValue(application.getString(R.string.lock_phone, it))
            else _displayText.postValue(phoneLockingDisabledString)
        }
    }

    override fun onCleared() {
        super.onCleared()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    fun requestLockPhone() {
        val phoneLockingEnabled = phoneLockingEnabled.value == true
        val isPhoneConnected = phoneConnected.value == true
        if (isPhoneConnected && phoneLockingEnabled) {
            ConfirmationActivityHandler.successAnimation(getApplication())
            messageClient.sendMessage(phoneId, LOCK_PHONE_PATH, null)
        } else if (!phoneLockingEnabled) {
            ConfirmationActivityHandler.failAnimation(getApplication(), phoneLockingDisabledString)
        } else {
            ConfirmationActivityHandler.failAnimation(getApplication())
        }
    }
}
