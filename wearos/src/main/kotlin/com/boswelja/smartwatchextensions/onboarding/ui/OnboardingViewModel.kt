package com.boswelja.smartwatchextensions.onboarding.ui

import android.app.Application
import androidx.annotation.VisibleForTesting
import androidx.datastore.core.DataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.PhoneState
import com.boswelja.smartwatchextensions.common.connection.Messages.WATCH_REGISTERED_PATH
import com.boswelja.smartwatchextensions.phoneStateStore
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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

    @VisibleForTesting
    val messageListener =
        MessageClient.OnMessageReceivedListener {
            when (it.path) {
                WATCH_REGISTERED_PATH -> {
                    viewModelScope.launch {
                        dataStore.updateData { state ->
                            state.copy(id = it.sourceNodeId)
                        }
                    }
                }
            }
        }

    fun watchName() = flow {
        val node = nodeClient.localNode.await()
        emit(node.displayName)
    }

    init {
        messageClient.addListener(messageListener)
    }

    override fun onCleared() {
        super.onCleared()
        messageClient.removeListener(messageListener)
    }
}
