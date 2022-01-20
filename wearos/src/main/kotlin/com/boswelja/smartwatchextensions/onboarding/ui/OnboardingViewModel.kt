package com.boswelja.smartwatchextensions.onboarding.ui

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.devicemanagement.PhoneState
import com.boswelja.smartwatchextensions.devicemanagement.phoneStateStore
import com.boswelja.watchconnection.wear.discovery.DiscoveryClient
import com.boswelja.watchconnection.wear.message.MessageClient
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

/**
 * A [AndroidViewModel] to provide data to onboarding Composables.
 */
class OnboardingViewModel internal constructor(
    application: Application,
    private val discoveryClient: DiscoveryClient,
    private val messageClient: MessageClient,
    private val dataStore: DataStore<PhoneState>
) : AndroidViewModel(application) {

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        DiscoveryClient(application),
        MessageClient(application),
        application.phoneStateStore
    )

    /**
     * Retrieve the local device name.
     */
    fun watchName() = flow {
        val node = discoveryClient.localWatch()
        emit(node.name)
    }

    init {
        viewModelScope.launch {
            messageClient.incomingMessages().collect {
                dataStore.updateData { state ->
                    state.copy(id = it.sourceUid)
                }
            }
        }
    }
}
