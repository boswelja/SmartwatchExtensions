package com.boswelja.smartwatchextensions.main.ui

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.capability.CapabilityUpdater
import com.boswelja.smartwatchextensions.core.devicemanagement.PhoneState
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * A [ViewModel] that provides data for [MainActivity].
 */
class MainViewModel(
    dataStore: DataStore<PhoneState>,
    private val capabilityUpdater: CapabilityUpdater
) : ViewModel() {

    /**
     * Flow whether this watch is registered
     */
    val isRegistered = dataStore.data.map { it.id.isNotBlank() }

    init {
        viewModelScope.launch {
            capabilityUpdater.updateCapabilities()
        }
    }
}
