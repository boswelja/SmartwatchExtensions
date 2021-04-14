package com.boswelja.smartwatchextensions.extensions.ui

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.ConfirmationActivityHandler
import com.boswelja.smartwatchextensions.PhoneState
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.batterysync.References.REQUEST_BATTERY_UPDATE_PATH
import com.boswelja.smartwatchextensions.common.connection.Messages
import com.boswelja.smartwatchextensions.extensions.ExtensionSettings
import com.boswelja.smartwatchextensions.extensions.extensionSettingsStore
import com.boswelja.smartwatchextensions.phoneStateStore
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class ExtensionsViewModel internal constructor(
    application: Application,
    private val messageClient: MessageClient,
    private val nodeClient: NodeClient,
    phoneStateStore: DataStore<PhoneState>,
    extensionSettingsStore: DataStore<ExtensionSettings>
) : AndroidViewModel(application) {

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        Wearable.getMessageClient(application),
        Wearable.getNodeClient(application),
        application.phoneStateStore,
        application.extensionSettingsStore
    )

    private val phoneId = phoneStateStore.data.map { it.id }

    private val _phoneConnected = MutableLiveData(false)

    val phoneLockingEnabled =
        extensionSettingsStore.data.map { it.phoneLockingEnabled }.asLiveData()
    val batterySyncEnabled = extensionSettingsStore.data.map { it.batterySyncEnabled }.asLiveData()

    val batteryPercent = phoneStateStore.data.map { it.batteryPercent }.asLiveData()
    val phoneName = phoneStateStore.data.map { it.name }.asLiveData()
    val phoneConnected: LiveData<Boolean>
        get() = _phoneConnected

    init {
        checkPhoneConnection()
    }

    fun checkPhoneConnection() {
        viewModelScope.launch(Dispatchers.IO) {
            val id = phoneId.first()
            Timber.d("Checking phone with ID %s is connected", id)
            val connectedNodes = nodeClient.connectedNodes.await()
            val isPhoneConnected = connectedNodes.any { node -> node.id == id && node.isNearby }
            _phoneConnected.postValue(isPhoneConnected)
            Timber.d("isPhoneConnected = %s", isPhoneConnected)
        }
    }

    fun updateBatteryStats() {
        val isBatterySyncEnabled = batterySyncEnabled.value == true
        val isPhoneConnected = phoneConnected.value == true
        if (isPhoneConnected && isBatterySyncEnabled) {
            ConfirmationActivityHandler.successAnimation(getApplication())
            viewModelScope.launch {
                phoneId.collect {
                    messageClient.sendMessage(it, REQUEST_BATTERY_UPDATE_PATH, null)
                }
            }
        } else if (!isBatterySyncEnabled) {
            ConfirmationActivityHandler.failAnimation(
                getApplication(),
                getApplication<Application>().getString(R.string.battery_sync_disabled)
            )
        } else {
            ConfirmationActivityHandler.failAnimation(
                getApplication(),
                getApplication<Application>().getString(R.string.phone_not_connected)
            )
        }
    }

    fun requestLockPhone() {
        val phoneLockingEnabled = phoneLockingEnabled.value == true
        val isPhoneConnected = phoneConnected.value == true
        if (isPhoneConnected && phoneLockingEnabled) {
            ConfirmationActivityHandler.successAnimation(getApplication())
            viewModelScope.launch {
                phoneId.collect {
                    messageClient.sendMessage(it, Messages.LOCK_PHONE, null)
                }
            }
        } else if (!phoneLockingEnabled) {
            ConfirmationActivityHandler.failAnimation(
                getApplication(),
                getApplication<Application>().getString(R.string.lock_phone_disabled)
            )
        } else {
            ConfirmationActivityHandler.failAnimation(
                getApplication(),
                getApplication<Application>().getString(R.string.phone_not_connected)
            )
        }
    }
}
