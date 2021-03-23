package com.boswelja.devicemanager.onboarding.ui

import android.app.Application
import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.connection.Messages.CHECK_WATCH_REGISTERED_PATH
import com.boswelja.devicemanager.common.connection.Messages.WATCH_REGISTERED_PATH
import com.boswelja.devicemanager.phoneconnectionmanager.References.PHONE_ID_KEY
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.Wearable

class OnboardingViewModel
@JvmOverloads
constructor(
    application: Application,
    private val nodeClient: NodeClient = Wearable.getNodeClient(application),
    private val messageClient: MessageClient = Wearable.getMessageClient(application),
    private val sharedPreferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(application)
) : AndroidViewModel(application) {

    @VisibleForTesting
    val messageListener =
        MessageClient.OnMessageReceivedListener {
            when (it.path) {
                WATCH_REGISTERED_PATH -> {
                    sharedPreferences.edit { putString(PHONE_ID_KEY, it.sourceNodeId) }
                    onWatchRegistered.postValue(true)
                }
            }
        }

    val onWatchRegistered = MutableLiveData(false)

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
