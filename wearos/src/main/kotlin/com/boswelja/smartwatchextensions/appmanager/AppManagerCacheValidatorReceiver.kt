package com.boswelja.smartwatchextensions.appmanager

import android.content.Context
import com.boswelja.watchconnection.common.message.ReceivedMessage
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
            context.sendAllApps(messageClient, discoveryClient)
        }
    }
}
