package com.boswelja.devicemanager.onboarding.ui

import android.app.Application
import androidx.annotation.VisibleForTesting
import androidx.datastore.core.DataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.boswelja.devicemanager.PhoneState
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.connection.Messages.CHECK_WATCH_REGISTERED_PATH
import com.boswelja.devicemanager.common.connection.Messages.WATCH_REGISTERED_PATH
import com.boswelja.devicemanager.phoneStateStore
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.launch

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

    private val _localName = MutableLiveData<String?>(null)
    val setupNameText =
        Transformations.map(_localName) { it ?: application.getString(R.string.error) }

    init {
        messageClient.addListener(messageListener)
        refreshLocalName()
    }

    override fun onCleared() {
        super.onCleared()
        messageClient.removeListener(messageListener)
    }

    private fun refreshLocalName() {
        nodeClient.localNode.addOnCompleteListener { _localName.postValue(it.result?.displayName) }
    }

    fun refreshRegisteredStatus() {
        nodeClient.connectedNodes.addOnSuccessListener {
            it.firstOrNull()?.id?.let { id ->
                messageClient.sendMessage(
                    id,
                    CHECK_WATCH_REGISTERED_PATH,
                    null
                )
            }
        }
    }
}
