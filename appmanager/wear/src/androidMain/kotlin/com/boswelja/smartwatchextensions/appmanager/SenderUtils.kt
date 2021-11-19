package com.boswelja.smartwatchextensions.appmanager

import android.content.Context
import com.boswelja.watchconnection.common.message.Message
import com.boswelja.watchconnection.serialization.MessageHandler
import com.boswelja.watchconnection.wear.discovery.DiscoveryClient
import com.boswelja.watchconnection.wear.message.MessageClient

/**
 * Send all apps installed to the companion phone with a given ID.
 * @param messageClient The [MessageClient] instance to use.
 */
suspend fun Context.sendAllApps(
    messageClient: MessageClient,
    discoveryClient: DiscoveryClient
) {
    val pairedPhone = discoveryClient.pairedPhone()!!

    val messageHandler = MessageHandler(AppListSerializer, messageClient)
    // Get all current packages
    val allApps = getAllApps()

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
