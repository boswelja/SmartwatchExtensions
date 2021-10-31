package com.boswelja.smartwatchextensions.devicemanagement

import android.content.Context
import com.boswelja.smartwatchextensions.capability.CapabilityUpdater
import com.boswelja.smartwatchextensions.phoneStateStore
import com.boswelja.watchconection.common.message.MessageReceiver
import com.boswelja.watchconnection.common.message.Message
import com.boswelja.watchconnection.common.message.ReceivedMessage
import com.boswelja.watchconnection.wear.discovery.DiscoveryClient
import com.boswelja.watchconnection.wear.message.MessageClient
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * A [MessageReceiver] for receiving watch registration messages.
 */
class WatchRegistrationReceiver : MessageReceiver<Unit?>(RegistrationSerializer), KoinComponent {

    private val capabilityUpdater: CapabilityUpdater by inject()
    private val messageClient: MessageClient by inject()
    private val discoveryClient: DiscoveryClient by inject()

    override suspend fun onMessageReceived(context: Context, message: ReceivedMessage<Unit?>) {
        capabilityUpdater.updateCapabilities()
        val phone = discoveryClient.pairedPhone()!!
        context.phoneStateStore.updateData {
            it.copy(id = message.sourceUid, name = phone.name)
        }
        messageClient.sendMessage(
            phone,
            Message(
                WATCH_REGISTERED_PATH,
                null
            )
        )
    }
}
