package com.boswelja.smartwatchextensions.appmanager

import android.content.Context
import com.boswelja.watchconnection.common.message.ReceivedMessage
import com.boswelja.watchconnection.serialization.MessageReceiver

/**
 * A [MessageReceiver] to handle App Manager commands sent to the device.
 */
class AppManagerCommandReceiver : MessageReceiver<String>(PackageNameSerializer) {
    override suspend fun onMessageReceived(context: Context, message: ReceivedMessage<String>) {
        val intent = when (message.path) {
            REQUEST_UNINSTALL_PACKAGE -> context.packageManager.requestUninstallIntent(message.data)
            REQUEST_OPEN_PACKAGE -> context.packageManager.launchIntent(message.data)
            else -> null
        }
        context.startActivity(intent)
    }
}
