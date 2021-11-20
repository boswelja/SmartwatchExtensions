package com.boswelja.smartwatchextensions.appmanager

import android.content.Context
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.graphics.drawable.toBitmap
import com.boswelja.watchconnection.common.message.Message
import com.boswelja.watchconnection.common.message.ReceivedMessage
import com.boswelja.watchconnection.serialization.MessageHandler
import com.boswelja.watchconnection.serialization.MessageReceiver
import com.boswelja.watchconnection.wear.message.MessageClient
import okio.ByteString.Companion.toByteString
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * A [MessageReceiver] for receiving app cache validation requests.
 */
class AppManagerCacheValidatorReceiver :
    MessageReceiver<AppVersions>(CacheValidationSerializer),
    KoinComponent {

    private val messageClient: MessageClient by inject()

    override suspend fun onMessageReceived(
        context: Context,
        message: ReceivedMessage<AppVersions>
    ) {
        // Get a list of apps installed on this device, and format for cache validation.
        val currentPackages = context.packageManager.getInstalledPackages(0)
            .map { AppVersion(it.packageName, PackageInfoCompat.getLongVersionCode(it)) }

        // Get the hash code for our local app list, and check against the remote cache
        val cacheValid = areVersionsEqual(
            currentPackages,
            message.data.versions
        )
        if (!cacheValid) {
            // Get all current packages
            val allApps = context.getAllApps()
            sendAllApps(message.sourceUid, allApps)
            sendAllIcons(context, message.sourceUid, allApps)
        }
    }

    /**
     * Send all apps installed to the companion phone with a given ID.
     * @param allApps The list of apps to send.
     */
    private suspend fun sendAllApps(
        targetUid: String,
        allApps: List<App>
    ) {
        val messageHandler = MessageHandler(AppListSerializer, messageClient)

        // Let the phone know what we're doing
        messageClient.sendMessage(
            targetUid,
            Message(APP_SENDING_START, null)
        )

        // Send all apps
        messageHandler.sendMessage(
            targetUid,
            Message(
                APP_LIST,
                AppList(allApps)
            )
        )

        // Send a message notifying the phone of a successful operation
        messageClient.sendMessage(
            targetUid,
            Message(APP_SENDING_COMPLETE, null)
        )
    }

    private suspend fun sendAllIcons(
        context: Context,
        targetUid: String,
        allApps: List<App>
    ) {
        val messageHandler = MessageHandler(AppIconSerializer, messageClient)
        allApps.forEach { app ->
            try {
                // Load icon
                val drawable = context.packageManager.getApplicationIcon(app.packageName)
                val bitmap = drawable.toBitmap()
                val bytes = bitmap.toByteArray()
                messageHandler.sendMessage(
                    targetUid,
                    Message(
                        APP_ICON,
                        AppIcon(
                            app.packageName,
                            bytes.toByteString()
                        )
                    )
                )
            } catch (_: Exception) { }
        }
    }

    private fun areVersionsEqual(
        localVersions: List<AppVersion>,
        remoteVersions: List<AppVersion>
    ): Boolean {
        if (localVersions.count() != remoteVersions.count()) return false
        return localVersions.containsAll(remoteVersions)
    }
}
