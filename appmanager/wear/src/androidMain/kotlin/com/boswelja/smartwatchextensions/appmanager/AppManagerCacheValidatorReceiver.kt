package com.boswelja.smartwatchextensions.appmanager

import android.content.Context
import com.boswelja.watchconnection.common.message.Message
import com.boswelja.watchconnection.common.message.ReceivedMessage
import com.boswelja.watchconnection.serialization.MessageHandler
import com.boswelja.watchconnection.serialization.MessageReceiver
import com.boswelja.watchconnection.wear.discovery.DiscoveryClient
import com.boswelja.watchconnection.wear.message.MessageClient
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * A [MessageReceiver] for receiving app cache validation requests.
 */
class AppManagerCacheValidatorReceiver :
    MessageReceiver<Int>(CacheValidationSerializer),
    KoinComponent {

    private val messageClient: MessageClient by inject()
    private val discoveryClient: DiscoveryClient by inject()

    override suspend fun onMessageReceived(context: Context, message: ReceivedMessage<Int>) {
        // Get a list of apps installed on this device, and format for cache validation.
        val currentPackages = context.packageManager.getInstalledPackages(0)
            .map { it.packageName to it.lastUpdateTime }

        // Get the hash code for our local app list, and check against the remote cache
        val currentHash = CacheValidation.getHashCode(currentPackages)
        if (message.data != currentHash) {
            sendAllApps(context)
        }
    }

    /**
     * Send all apps installed to the companion phone with a given ID.
     * @param context [Context].
     */
    private suspend fun sendAllApps(
        context: Context
    ) {
        val pairedPhone = discoveryClient.pairedPhone()!!

        val messageHandler = MessageHandler(AppListSerializer, messageClient)
        // Get all current packages
        val allApps = context.getAllApps()

        // Let the phone know what we're doing
        messageClient.sendMessage(
            pairedPhone.uid,
            Message(APP_SENDING_START, null)
        )

        // Send all apps
        messageHandler.sendMessage(
            pairedPhone.uid,
            Message(
                APP_LIST,
                AppList(allApps)
            )
        )

        // Send a message notifying the phone of a successful operation
        messageClient.sendMessage(
            pairedPhone.uid,
            Message(APP_SENDING_COMPLETE, null)
        )
    }

}
