package com.boswelja.smartwatchextensions.main.ui

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.PhoneState
import com.boswelja.smartwatchextensions.capability.CapabilityUpdater
import com.boswelja.watchconnection.wear.discovery.DiscoveryClient
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainViewModel(
    dataStore: DataStore<PhoneState>,
    private val discoveryClient: DiscoveryClient,
    private val capabilityUpdater: CapabilityUpdater
) : ViewModel() {

    val isRegistered = dataStore.data.map { it.id.isNotBlank() }

    init {
        viewModelScope.launch {
            capabilityUpdater.updateCapabilities()
        }
    }
}
