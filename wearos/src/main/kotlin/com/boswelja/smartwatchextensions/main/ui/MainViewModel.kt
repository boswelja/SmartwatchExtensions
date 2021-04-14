package com.boswelja.smartwatchextensions.main.ui

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import com.boswelja.smartwatchextensions.PhoneState
import com.boswelja.smartwatchextensions.capability.CapabilityUpdater
import com.boswelja.smartwatchextensions.phoneStateStore
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
