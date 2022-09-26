package com.boswelja.smartwatchextensions.appmanager

import android.content.Context
import com.boswelja.watchconnection.common.message.MessageReceiver
import com.boswelja.watchconnection.common.message.ReceivedMessage

/**
 * A [MessageReceiver] to handle App Manager commands sent to the device.
 */
class AppManagerCommandReceiver : MessageReceiver() {
    override suspend fun onMessageReceived(context: Context, message: ReceivedMessage<ByteArray?>) {
        if (message.data == null) return

        val intent = when (message.path) {
            RequestUninstallPackage -> {
                val packageName = PackageNameSerializer.deserialize(message.data!!)
                context.packageManager.requestUninstallIntent(packageName)
            }
            RequestOpenPackage -> {
                val packageName = PackageNameSerializer.deserialize(message.data!!)
                context.packageManager.launchIntent(packageName)
            }
            else -> null
        }
        if (intent != null) context.startActivity(intent)
    }
}
