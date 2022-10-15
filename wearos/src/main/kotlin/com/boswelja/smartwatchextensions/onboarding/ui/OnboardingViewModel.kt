package com.boswelja.smartwatchextensions.onboarding.ui

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.core.devicemanagement.PhoneState
import com.boswelja.smartwatchextensions.core.devicemanagement.phoneStateStore
import com.boswelja.smartwatchextensions.wearable.ext.receiveMessages
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * A [AndroidViewModel] to provide data to onboarding Composables.
 */
class OnboardingViewModel internal constructor(
    application: Application,
    private val nodeClient: NodeClient,
    private val messageClient: MessageClient,
    private val dataStore: DataStore<PhoneState>
) : AndroidViewModel(application) {

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        Wearable.getNodeClient(application),
        Wearable.getMessageClient(application),
        application.phoneStateStore
    )

    /**
     * Retrieve the local device name.
     */
    fun watchName() = flow {
        val node = nodeClient.localNode.await()
        emit(node.displayName)
    }

    init {
        viewModelScope.launch {
            messageClient.receiveMessages().collect {
                dataStore.updateData { state ->
                    state.copy(id = it.sourceNodeId)
                }
            }
        }
    }
}
