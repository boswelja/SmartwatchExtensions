/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
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
import com.boswelja.devicemanager.common.Event
import com.boswelja.devicemanager.common.setup.References
import com.boswelja.devicemanager.common.setup.References.CHECK_WATCH_REGISTERED_PATH
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
                References.WATCH_REGISTERED_PATH -> {
                    sharedPreferences.edit { putString(PHONE_ID_KEY, it.sourceNodeId) }
                    onWatchRegistered.fire()
                }
            }
        }

    val onWatchRegistered = Event()

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
