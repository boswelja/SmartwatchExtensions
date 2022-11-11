package com.boswelja.smartwatchextensions.extensions.ui

import androidx.datastore.core.DataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.core.devicemanagement.PhoneState
import com.boswelja.smartwatchextensions.phonelocking.LockPhone
import com.boswelja.smartwatchextensions.phonelocking.PhoneLockingStateRepository
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wearable.MessageClient
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.tasks.await

/**
 * A [AndroidViewModel] for providing data to Extension Composables.
 */
class ExtensionsViewModel internal constructor(
    private val messageClient: MessageClient,
    private val phoneStateStore: DataStore<PhoneState>,
    phoneLockingStateRepository: PhoneLockingStateRepository
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
     * Request locking the connected device.
     * @return true if the request was sent successfully, false otherwise.
     */
    suspend fun requestLockPhone(): Boolean {
        val phoneId = phoneStateStore.data.map { it.id }.first()
        return try {
            messageClient.sendMessage(phoneId, LockPhone, null).await()
            true
        } catch (_: ApiException) {
            false
        }
    }
}
