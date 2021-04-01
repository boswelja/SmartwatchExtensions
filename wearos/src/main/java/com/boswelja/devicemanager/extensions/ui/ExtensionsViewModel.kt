package com.boswelja.devicemanager.extensions.ui

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.ConfirmationActivityHandler
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.batterysync.References.REQUEST_BATTERY_UPDATE_PATH
import com.boswelja.devicemanager.common.connection.Messages
import com.boswelja.devicemanager.common.preference.PreferenceKey.BATTERY_PERCENT_KEY
import com.boswelja.devicemanager.common.preference.PreferenceKey.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.devicemanager.common.preference.PreferenceKey.PHONE_LOCKING_ENABLED_KEY
import com.boswelja.devicemanager.phoneconnectionmanager.References.PHONE_ID_KEY
import com.boswelja.devicemanager.phoneconnectionmanager.References.PHONE_NAME_KEY
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class ExtensionsViewModel internal constructor(
    application: Application,
    private val messageClient: MessageClient,
    private val nodeClient: NodeClient,
    private val sharedPreferences: SharedPreferences
) : AndroidViewModel(application) {

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        Wearable.getMessageClient(application),
        Wearable.getNodeClient(application),
        PreferenceManager.getDefaultSharedPreferences(application)
    )

    private val phoneId = sharedPreferences.getString(PHONE_ID_KEY, "") ?: ""

    private val preferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            when (key) {
                BATTERY_SYNC_ENABLED_KEY ->
                    _batterySyncEnabled.postValue(sharedPreferences.getBoolean(key, false))
                BATTERY_PERCENT_KEY -> _batteryPercent.postValue(sharedPreferences.getInt(key, 0))
                PHONE_LOCKING_ENABLED_KEY ->
                    _phoneLockingEnabled.postValue(sharedPreferences.getBoolean(key, false))
                PHONE_NAME_KEY ->
                    _phoneName.postValue(
                        sharedPreferences.getString(
                            key, application.getString(R.string.default_phone_name)
                        )
                    )
            }
        }

    private val _phoneConnected = MutableLiveData(false)
    private val _batterySyncEnabled =
        MutableLiveData(sharedPreferences.getBoolean(BATTERY_SYNC_ENABLED_KEY, false))
    private val _phoneName =
        MutableLiveData(
            sharedPreferences.getString(
                PHONE_NAME_KEY, application.getString(R.string.default_phone_name)
            )
                ?: application.getString(R.string.default_phone_name)
        )
    private val _batteryPercent = MutableLiveData(sharedPreferences.getInt(BATTERY_PERCENT_KEY, 0))
    private val _phoneLockingEnabled =
        MutableLiveData(sharedPreferences.getBoolean(PHONE_LOCKING_ENABLED_KEY, false))

    val phoneLockingEnabled: LiveData<Boolean>
        get() = _phoneLockingEnabled
    val batteryPercent: LiveData<Int>
        get() = _batteryPercent
    val phoneName: LiveData<String>
        get() = _phoneName
    val batterySyncEnabled: LiveData<Boolean>
        get() = _batterySyncEnabled
    val phoneConnected: LiveData<Boolean>
        get() = _phoneConnected

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
        checkPhoneConnection()
    }

    override fun onCleared() {
        super.onCleared()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    fun checkPhoneConnection() {
        viewModelScope.launch(Dispatchers.IO) {
            Timber.d("Checking phone with ID %s is connected", phoneId)
            val connectedNodes = Tasks.await(nodeClient.connectedNodes)
            val isPhoneConnected =
                connectedNodes.any { node -> node.id == phoneId && node.isNearby }
            _phoneConnected.postValue(isPhoneConnected)
            Timber.d("isPhoneConnected = %s", isPhoneConnected)
        }
    }

    fun updateBatteryStats() {
        val isBatterySyncEnabled = batterySyncEnabled.value == true
        val isPhoneConnected = phoneConnected.value == true
        if (isPhoneConnected && isBatterySyncEnabled) {
            ConfirmationActivityHandler.successAnimation(getApplication())
            messageClient.sendMessage(phoneId, REQUEST_BATTERY_UPDATE_PATH, null)
        } else if (!isBatterySyncEnabled) {
            ConfirmationActivityHandler.failAnimation(
                getApplication(),
                getApplication<Application>().getString(R.string.battery_sync_disabled)
            )
        } else {
            ConfirmationActivityHandler.failAnimation(getApplication())
        }
    }

    fun requestLockPhone() {
        val phoneLockingEnabled = phoneLockingEnabled.value == true
        val isPhoneConnected = phoneConnected.value == true
        if (isPhoneConnected && phoneLockingEnabled) {
            ConfirmationActivityHandler.successAnimation(getApplication())
            messageClient.sendMessage(phoneId, Messages.LOCK_PHONE, null)
        } else if (!phoneLockingEnabled) {
            ConfirmationActivityHandler.failAnimation(
                getApplication(),
                getApplication<Application>().getString(R.string.lock_phone_disabled)
            )
        } else {
            ConfirmationActivityHandler.failAnimation(getApplication())
        }
    }
}
