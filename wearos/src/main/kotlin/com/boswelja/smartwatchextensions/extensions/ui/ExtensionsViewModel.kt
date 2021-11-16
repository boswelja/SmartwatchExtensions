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

/**
 * A [AndroidViewModel] for providing data to Extension Composables.
 */
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
        MessageClient(application),
        DiscoveryClient(application),
        application.phoneStateStore,
        BatteryStatsStore(application.batteryStatsStore),
        application.extensionSettingsStore
    )

    /**
     * Flow whether phone locking is enabled.
     */
    val phoneLockingEnabled = extensionSettingsStore.data.map { it.phoneLockingEnabled }

    /**
     * Flow whether Battery Sync is enabled.
     */
    val batterySyncEnabled = extensionSettingsStore.data.map { it.batterySyncEnabled }

    /**
     * Flow the connected phone's battery percent.
     */
    val batteryPercent = batteryStatsStore.getStatsForPhone().map { it.percent }

    /**
     * Flow the paired phone name.
     */
    val phoneName = phoneStateStore.data.map { it.name }

    /**
     * Flow whether the paired phone is connected.
     */
    fun phoneConnected() = discoveryClient.connectionMode()
        .map { it != ConnectionMode.Disconnected }

    /**
     * Request updated battery stats from the connected device.
     * @return true if the request was sent successfully, false otherwise.
     */
    suspend fun requestBatteryStats(): Boolean {
        if (phoneConnected().first()) {
            val phoneId = discoveryClient.pairedPhone()!!.uid
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
        if (phoneConnected().first()) {
            val phoneId = discoveryClient.pairedPhone()!!.uid
            return messageClient.sendMessage(phoneId, Message(LOCK_PHONE, null))
        }
        return false
    }
}
