package com.boswelja.smartwatchextensions.phoneconnectionmanager

import android.content.Context
import com.boswelja.smartwatchextensions.capability.CapabilityUpdater
import com.boswelja.smartwatchextensions.core.devicemanagement.RequestUpdateCapabilities
import com.boswelja.smartwatchextensions.core.settings.ResetSettings
import com.boswelja.smartwatchextensions.extensions.ExtensionSettings
import com.boswelja.smartwatchextensions.extensions.extensionSettingsStore
import com.boswelja.watchconnection.common.message.MessageReceiver
import com.boswelja.watchconnection.common.message.ReceivedMessage
import com.boswelja.watchconnection.wear.discovery.DiscoveryClient
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * A [MessageReceiver] for receiving empty messages.
 */
class MessageReceiver :
    MessageReceiver(),
    KoinComponent {

    private val discoveryClient: DiscoveryClient by inject()

    override suspend fun onMessageReceived(context: Context, message: ReceivedMessage<ByteArray?>) {
        when (message.path) {
            ResetSettings -> {
                // TODO Add separate feature states
                context.extensionSettingsStore.updateData {
                    // Recreate the DataStore with default values
                    ExtensionSettings(
                        phoneSeparationNotis = false
                    )
                }
            }
            RequestUpdateCapabilities -> {
                CapabilityUpdater(context, discoveryClient).updateCapabilities()
            }
        }
    }
}
