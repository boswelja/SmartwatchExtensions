package com.boswelja.smartwatchextensions.phoneconnectionmanager

import com.boswelja.smartwatchextensions.capability.CapabilityUpdater
import com.boswelja.smartwatchextensions.core.devicemanagement.RequestUpdateCapabilities
import com.boswelja.smartwatchextensions.core.settings.ResetSettings
import com.boswelja.watchconnection.common.message.MessageReceiver
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import org.koin.android.ext.android.inject

/**
 * A [MessageReceiver] for receiving empty messages.
 */
class MessageReceiver : WearableListenerService() {

    private val capabilityClient: CapabilityClient by inject()

    override fun onMessageReceived(message: MessageEvent) {
        when (message.path) {
            ResetSettings -> {
                // TODO Add separate feature states
            }
            RequestUpdateCapabilities -> {
                CapabilityUpdater(this, capabilityClient).updateCapabilities()
            }
        }
    }
}
