package com.boswelja.devicemanager.main.ui

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import com.boswelja.devicemanager.PhoneState
import com.boswelja.devicemanager.capability.CapabilityUpdater
import com.boswelja.devicemanager.phoneStateStore
import kotlinx.coroutines.flow.map

class MainViewModel internal constructor(
    application: Application,
    dataStore: DataStore<PhoneState>
) : AndroidViewModel(application) {

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        application.phoneStateStore
    )

    val isRegistered = dataStore.data.map { it.id }.asLiveData().map { it.isNotBlank() }

    init {
        CapabilityUpdater(application).updateCapabilities()
    }
}
