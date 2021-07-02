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
import com.boswelja.smartwatchextensions.phoneconnectionmanager.ConnectionHelper
import com.boswelja.smartwatchextensions.phoneconnectionmanager.Status
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch

class ExtensionsViewModel internal constructor(
    application: Application,
    private val messageClient: MessageClient,
    private val connectionHelper: ConnectionHelper,
    phoneStateStore: DataStore<PhoneState>,
    extensionSettingsStore: DataStore<ExtensionSettings>
) : AndroidViewModel(application) {

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        Wearable.getMessageClient(application),
        ConnectionHelper(application, refreshInterval = 15000),
        application.phoneStateStore,
        application.extensionSettingsStore
    )

    private val phoneId = phoneStateStore.data.map { it.id }

    val phoneLockingEnabled = extensionSettingsStore.data.map { it.phoneLockingEnabled }
    val batterySyncEnabled = extensionSettingsStore.data.map { it.batterySyncEnabled }

    val batteryPercent = phoneStateStore.data.map { it.batteryPercent }
    val phoneName = phoneStateStore.data.map { it.name }

    fun phoneStatus() = connectionHelper.phoneStatus()

    fun updateBatteryStats() {
        viewModelScope.launch {
            val isBatterySyncEnabled = batterySyncEnabled.first()
            val isPhoneConnected = isPhoneConnected()
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

    @ExperimentalCoroutinesApi
    fun requestLockPhone() {
        viewModelScope.launch {
            val phoneLockingEnabled = phoneLockingEnabled.first()
            val isPhoneConnected = isPhoneConnected()
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

    @ExperimentalCoroutinesApi
    private suspend fun isPhoneConnected(): Boolean {
        return phoneStatus().mapLatest {
            it == Status.CONNECTED_NEARBY || it == Status.CONNECTED
        }.first()
    }
}
