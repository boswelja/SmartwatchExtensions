package com.boswelja.devicemanager.main.ui

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.capability.CapabilityUpdater
import com.boswelja.devicemanager.phoneconnectionmanager.References.PHONE_ID_KEY

class MainViewModel internal constructor(
    application: Application,
    private val sharedPreferences: SharedPreferences
) : AndroidViewModel(application) {

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        PreferenceManager.getDefaultSharedPreferences(application)
    )

    private val registrationObserver =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == PHONE_ID_KEY) {
                _isRegistered.postValue(
                    !sharedPreferences.getString(PHONE_ID_KEY, "").isNullOrBlank()
                )
            }
        }

    private val _isRegistered =
        MutableLiveData(!sharedPreferences.getString(PHONE_ID_KEY, "").isNullOrBlank())

    val isRegistered: LiveData<Boolean>
        get() = _isRegistered

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(registrationObserver)
        CapabilityUpdater(application).updateCapabilities()
    }

    override fun onCleared() {
        super.onCleared()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(registrationObserver)
    }
}
