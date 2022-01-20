package com.boswelja.smartwatchextensions.batterysync.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.batterysync.BatteryStatsRepository
import com.boswelja.smartwatchextensions.batterysync.BatterySyncStateRepository
import com.boswelja.smartwatchextensions.batterysync.REQUEST_BATTERY_UPDATE_PATH
import com.boswelja.watchconnection.common.discovery.ConnectionMode
import com.boswelja.watchconnection.common.message.Message
import com.boswelja.watchconnection.wear.discovery.DiscoveryClient
import com.boswelja.watchconnection.wear.message.MessageClient
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * A ViewModel to provide data for the Battery Stats UI.
 */
class BatteryStatsViewModel(
    batteryStatsRepository: BatteryStatsRepository,
    batterySyncStateRepository: BatterySyncStateRepository,
    private val messageClient: MessageClient,
    private val discoveryClient: DiscoveryClient
) : ViewModel() {

    /**
     * Flow whether the paired phone is connected.
     */
    private val phoneConnected = discoveryClient.connectionMode()
        .map { it != ConnectionMode.Disconnected }

    /**
     * Flow whether Battery Sync is enabled.
     */
    val batterySyncEnabled = batterySyncStateRepository.getBatterySyncState()
        .map { it.batterySyncEnabled }
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            false
        )

    /**
     * Flow the paired phone's battery stats, or null if nothing has been received yet.
     */
    val batteryStats = batteryStatsRepository.getPhoneBatteryStats()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(stopTimeoutMillis = 1500),
            null
        )

    /**
     * Request updated battery stats from the connected device.
     * @return true if the request was sent successfully, false otherwise.
     */
    suspend fun trySyncBattery(): Boolean {
        if (phoneConnected.first()) {
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
}
