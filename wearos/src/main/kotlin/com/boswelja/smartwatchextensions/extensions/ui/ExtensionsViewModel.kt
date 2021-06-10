package com.boswelja.smartwatchextensions.extensions.ui

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.lifecycle.AndroidViewModel
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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

    private val _phoneConnected = MutableStateFlow(false)

    val phoneLockingEnabled = extensionSettingsStore.data.map { it.phoneLockingEnabled }
    val batterySyncEnabled = extensionSettingsStore.data.map { it.batterySyncEnabled }

    val batteryPercent = phoneStateStore.data.map { it.batteryPercent }
    val phoneName = phoneStateStore.data.map { it.name }
    val phoneConnected: Flow<Boolean>
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
            _phoneConnected.emit(isPhoneConnected)
            Timber.d("isPhoneConnected = %s", isPhoneConnected)
        }
    }

    fun updateBatteryStats() {
        viewModelScope.launch {
            val isBatterySyncEnabled = batterySyncEnabled.first()
            val isPhoneConnected = phoneConnected.first()
            if (isPhoneConnected && isBatterySyncEnabled) {
                ConfirmationActivityHandler.successAnimation(getApplication())
                val phoneId = phoneId.first()
                messageClient.sendMessage(phoneId, REQUEST_BATTERY_UPDATE_PATH, null)
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
    }

    fun requestLockPhone() {
        viewModelScope.launch {
            val phoneLockingEnabled = phoneLockingEnabled.first()
            val isPhoneConnected = phoneConnected.first()
            if (isPhoneConnected && phoneLockingEnabled) {
                ConfirmationActivityHandler.successAnimation(getApplication())
                val phoneId = phoneId.first()
                messageClient.sendMessage(phoneId, Messages.LOCK_PHONE, null)
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
}
