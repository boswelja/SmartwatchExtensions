package com.boswelja.smartwatchextensions.extensions.ui

import androidx.datastore.core.DataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.core.devicemanagement.PhoneState
import com.boswelja.smartwatchextensions.phonelocking.LockPhone
import com.boswelja.smartwatchextensions.phonelocking.PhoneLockingStateRepository
import com.boswelja.watchconnection.common.discovery.ConnectionMode
import com.boswelja.watchconnection.common.message.Message
import com.boswelja.watchconnection.wear.discovery.DiscoveryClient
import com.boswelja.watchconnection.wear.message.MessageClient
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * A [AndroidViewModel] for providing data to Extension Composables.
 */
class ExtensionsViewModel internal constructor(
    private val messageClient: MessageClient,
    private val discoveryClient: DiscoveryClient,
    phoneLockingStateRepository: PhoneLockingStateRepository,
    phoneStateStore: DataStore<PhoneState>
) : ViewModel() {

    /**
     * Flow whether phone locking is enabled.
     */
    val phoneLockingEnabled = phoneLockingStateRepository.getPhoneLockingState()
        .map { it.phoneLockingEnabled }
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            false
        )

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
     * Request locking the connected device.
     * @return true if the request was sent successfully, false otherwise.
     */
    suspend fun requestLockPhone(): Boolean {
        if (phoneConnected().first()) {
            val phoneId = discoveryClient.pairedPhone()!!.uid
            return messageClient.sendMessage(phoneId, Message(LockPhone, null))
        }
        return false
    }
}
