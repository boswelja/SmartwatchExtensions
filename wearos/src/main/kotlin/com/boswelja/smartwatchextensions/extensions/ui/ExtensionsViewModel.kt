package com.boswelja.smartwatchextensions.extensions.ui

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.lifecycle.AndroidViewModel
import com.boswelja.smartwatchextensions.PhoneState
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
import kotlinx.coroutines.tasks.await

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

    /**
     * Request updated battery stats from the connected device.
     * @return true if the request was sent successfully, false otherwise.
     */
    suspend fun requestBatteryStats(): Boolean {
        if (isPhoneConnected()) {
            val phoneId = phoneId.first()
            return try {
                messageClient.sendMessage(phoneId, REQUEST_BATTERY_UPDATE_PATH, null).await()
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
        return false
    }

    /**
     * Request locking the connected device.
     * @return true if the request was sent successfully, false otherwise.
     */
    suspend fun requestLockPhone(): Boolean {
        if (isPhoneConnected()) {
            val phoneId = phoneId.first()
            return try {
                messageClient.sendMessage(phoneId, Messages.LOCK_PHONE, null)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

        return false
    }

    /**
     * Gets the first value emitted by [ConnectionHelper.phoneStatus], and maps it to a Boolean
     * indicating whether the device is connected.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun isPhoneConnected(): Boolean {
        return phoneStatus().mapLatest {
            it == Status.CONNECTED_NEARBY || it == Status.CONNECTED
        }.first()
    }
}
