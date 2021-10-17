package com.boswelja.smartwatchextensions.main.ui

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.PhoneState
import com.boswelja.smartwatchextensions.capability.CapabilityUpdater
import com.boswelja.smartwatchextensions.phoneStateStore
import com.boswelja.watchconnection.wear.discovery.DiscoveryClient
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.instance

class MainViewModel internal constructor(
    application: Application,
    dataStore: DataStore<PhoneState>
) : AndroidViewModel(application), DIAware {

    override val di: DI by closestDI(application)

    private val discoveryClient: DiscoveryClient by instance()

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        application.phoneStateStore
    )

    val isRegistered = dataStore.data.map { it.id.isNotBlank() }

    init {
        viewModelScope.launch {
            CapabilityUpdater(application, discoveryClient).updateCapabilities()
        }
    }
}
