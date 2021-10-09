package com.boswelja.smartwatchextensions.extensions.ui

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.lifecycle.AndroidViewModel
import com.boswelja.smartwatchextensions.PhoneState
import com.boswelja.smartwatchextensions.batterysync.BatteryStatsStore
import com.boswelja.smartwatchextensions.batterysync.REQUEST_BATTERY_UPDATE_PATH
import com.boswelja.smartwatchextensions.batterysync.batteryStatsStore
import com.boswelja.smartwatchextensions.extensions.ExtensionSettings
import com.boswelja.smartwatchextensions.extensions.extensionSettingsStore
import com.boswelja.smartwatchextensions.phoneStateStore
import com.boswelja.smartwatchextensions.phonelocking.LOCK_PHONE
import com.boswelja.watchconnection.common.discovery.ConnectionMode
import com.boswelja.watchconnection.common.message.Message
import com.boswelja.watchconnection.wear.discovery.DiscoveryClient
import com.boswelja.watchconnection.wear.message.MessageClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class ExtensionsViewModel internal constructor(
    application: Application,
    private val messageClient: MessageClient,
    private val discoveryClient: DiscoveryClient,
    phoneStateStore: DataStore<PhoneState>,
    batteryStatsStore: BatteryStatsStore,
    extensionSettingsStore: DataStore<ExtensionSettings>
) : AndroidViewModel(application) {

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        MessageClient(application, listOf()),
        DiscoveryClient(application),
        application.phoneStateStore,
        BatteryStatsStore(application.batteryStatsStore),
        application.extensionSettingsStore
    )

    val phoneLockingEnabled = extensionSettingsStore.data.map { it.phoneLockingEnabled }
    val batterySyncEnabled = extensionSettingsStore.data.map { it.batterySyncEnabled }

    val batteryPercent = batteryStatsStore.getStatsForPhone().map { it.percent }
    val phoneName = phoneStateStore.data.map { it.name }

    fun phoneConected() = discoveryClient.connectionMode()
        .map { it != ConnectionMode.Disconnected }

    /**
     * Request updated battery stats from the connected device.
     * @return true if the request was sent successfully, false otherwise.
     */
    suspend fun requestBatteryStats(): Boolean {
        if (phoneConected().first()) {
            val phoneId = discoveryClient.pairedPhone()!!
            return messageClient.sendMessage(
                phoneId,
                Message(
                    REQUEST_BATTERY_UPDATE_PATH,
                    null
                )
            )
        }
        return false
    }

    /**
     * Request locking the connected device.
     * @return true if the request was sent successfully, false otherwise.
     */
    suspend fun requestLockPhone(): Boolean {
        if (phoneConected().first()) {
            val phoneId = discoveryClient.pairedPhone()!!
            return messageClient.sendMessage(phoneId, Message(LOCK_PHONE, null))
        }
        return false
    }
}
