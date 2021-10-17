package com.boswelja.smartwatchextensions.appmanager

import android.content.Context
import com.boswelja.watchconection.common.message.MessageReceiver
import com.boswelja.watchconnection.common.message.ReceivedMessage
import com.boswelja.watchconnection.wear.discovery.DiscoveryClient
import com.boswelja.watchconnection.wear.message.MessageClient
import org.kodein.di.DIAware
import org.kodein.di.LateInitDI
import org.kodein.di.instance

class AppManagerCacheValidatorReceiver :
    MessageReceiver<Int>(CacheValidationSerializer),
    DIAware {

    override val di = LateInitDI()

    private val messageClient: MessageClient by instance()
    private val discoveryClient: DiscoveryClient by instance()

    override suspend fun onMessageReceived(context: Context, message: ReceivedMessage<Int>) {
        di.baseDI = (context.applicationContext as DIAware).di

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
