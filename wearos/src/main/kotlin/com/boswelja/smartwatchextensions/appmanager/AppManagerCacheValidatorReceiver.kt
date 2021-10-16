package com.boswelja.smartwatchextensions.appmanager

import android.content.Context
import com.boswelja.watchconection.common.message.MessageReceiver
import com.boswelja.watchconnection.common.message.ReceivedMessage

class AppManagerCacheValidatorReceiver : MessageReceiver<Int>(CacheValidationSerializer) {
    override suspend fun onMessageReceived(context: Context, message: ReceivedMessage<Int>) {
        // Get a list of apps installed on this device, and format for cache validation.
        val currentPackages = context.packageManager.getInstalledPackages(0)
            .map { it.packageName to it.lastUpdateTime }

        // Get the hash code for our local app list, and check against the remote cache
        val currentHash = CacheValidation.getHashCode(currentPackages)
        if (message.data != currentHash) {
            context.sendAllApps()
        }
    }
}
